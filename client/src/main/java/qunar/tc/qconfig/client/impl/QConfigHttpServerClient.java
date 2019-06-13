package qunar.tc.qconfig.client.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.LineReader;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.FeatureRemote;
import qunar.tc.qconfig.client.exception.ResultUnexpectedException;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.EnvironmentAware;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static qunar.tc.qconfig.client.impl.QConfigEntryPoint.*;

/**
 * User: zhaohuiyu
 * Date: 5/15/14
 * Time: 5:40 PM
 */
class QConfigHttpServerClient implements QConfigServerClient {

    protected static final Logger logger = LoggerFactory.getLogger("qunar.tc.qconfig.client.impl.HttpClient");

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();

    private static final int LONG_POLLING_TIMEOUT = 90000;

    //系统启动的时候生成一个1000内的随机数，作为选择服务器的起点，避免所有客户端都访问同一台服务器。
    //之后如果访问这台服务器不出现问题，则会一直访问，只有当访问出现异常的时候才会跳到下一台。
    private static final AtomicInteger index = new AtomicInteger(new Random().nextInt(1000));

    protected QConfigEntryPoint entryPoint;

    QConfigHttpServerClient() {
        this.entryPoint = new QConfigEntryPoint();
    }

    protected String[] resolveUrls(String suffix, Feature feature) {
        boolean isHttps = feature != null && feature.isHttpsEnable();
        return entryPoint.resolveHttpUrl(suffix, isHttps);
    }

    @Override
    public ListenableFuture<TypedCheckResult> checkUpdate(Map<Meta, VersionProfile> files) {
        return new CheckUpdateFuture(this, entryPoint.resolveHttpUrl(CHECK_UPDATE_URL, false), files).request();
    }

    @Override
    public ListenableFuture<TypedCheckResult> loadGroupFiles() {
        return new CheckUpdateFuture(entryPoint.resolveHttpUrl(GET_GROUP_FILES, false), ServerManager.getInstance().getAppServerConfig().getName()).request();
    }

    @Override
    public ListenableFuture<TypedCheckResult> longPollingCheckUpdate(Map<Meta, VersionProfile> files) {
        return new LongPollingCheckUpdateFuture(this, entryPoint.resolveHttpUrl(LONG_POLLING_CHECK_UPDATE_URL, false), files).request();
    }

    @Override
    public ListenableFuture<Snapshot<String>> loadData(Meta key, VersionProfile version, Feature feature) {
        entryPoint.refresh();
        return new LoadDataFuture<Snapshot<String>>(this, resolveUrls(LOAD_DATA_URL, feature), key, version).request();
    }

    @Override
    public ListenableFuture<Snapshot<String>> forceReload(Meta key, long minVersion, Feature feature) {
        return new ForceReloadFuture(this, resolveUrls(FORCE_RELOAD_URL, feature), key, minVersion).request();
    }

    @Override
    public void recordLoading(ConfigLogType type, Meta meta, long version, String errorInfo) throws IOException {
        new RecordLoadingFuture(entryPoint.resolveHttpUrl(RECORD_LOADING_URL, false), type, meta, version, errorInfo).request();
    }

    private abstract static class RetryFuture<T> extends AbstractFuture<T> {
        private final String[] urls;
        private final int maxRetryTime;
        private int retries = 0;
        private QConfigHttpServerClient serverClient;

        RetryFuture(QConfigHttpServerClient serverClient, String[] urls) {
            this(urls, urls.length);
            this.serverClient = serverClient;
        }

        RetryFuture(String[] urls) {
            this(urls, urls.length);
        }

        RetryFuture(String[] urls, int maxRetryTime) {
            this.urls = urls;
            this.maxRetryTime = maxRetryTime;
        }

        protected abstract AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException;

        protected abstract boolean process(Response response);

        public RetryFuture<T> request() {
            if (retries++ > maxRetryTime) {
                setException(new ResultUnexpectedException("多次重试仍然无法访问qconfig server"));
                return this;
            }
            if (isDone()) return this;
            try {
                AsyncHttpClient.BoundRequestBuilder builder = buildRequest(select());
                String token = Tokens.getToken();
                if (Strings.isNullOrEmpty(token)) {
                    setException(new ResultUnexpectedException("请确保已经在注册了应用，并且在正确的位置配置了app id"));
                    return this;
                }

                //这里更改了设计， subEnv表示子环境，EnvName表示大环境，profile表示 env:subEnv
                builder.addHeader(Constants.TOKEN_NAME, token)
                        .addHeader(Constants.SUB_ENV, EnvironmentAware.determinedEnv())
                        .addHeader(Constants.PORT, String.valueOf(ServerManager.getInstance().getAppServerConfig().getPort()))
                        .addHeader(Constants.ENV_NAME, ServerManager.getInstance().getAppServerConfig().getEnv());
                Request request = builder.build();
                final com.ning.http.client.ListenableFuture<Response> future = HttpClientHolder.INSTANCE.executeRequest(request);

                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        Response response;
                        try {
                            response = future.get();
                        } catch (Exception e) {
                            logger.warn("get response error, try to failOf over", e);
                            failOver();
                            request();
                            return;
                        }

                        if (response.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                            String fileInfo = response.getHeader(Constants.FORBIDDEN_FILE);
                            String message;
                            if (Strings.isNullOrEmpty(fileInfo)) {
                                message = "身份校验失败";
                            } else {
                                message = "访问其他应用的配置需要将该配置设置为public[" + fileInfo + "]";
                            }
                            RuntimeException ex = new ResultUnexpectedException(response.getStatusCode(), message);
                            logger.warn(message);
                            setException(ex);
                            return;
                        }

                        if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                            setException(new ResultUnexpectedException(response.getStatusCode(), "请求参数错误"));
                            return;
                        }
                        if (!process(response)) {
                            failOver();
                            request();
                        }
                    }
                }, Constants.CURRENT_EXECUTOR);
            } catch (IOException e) {
                failOver();
                request();
            }
            return this;
        }

        private String select() {
            return urls[Math.abs(index.get() % urls.length)];
        }

        private void failOver() {
            if (serverClient != null) {
                serverClient.entryPoint.refresh();
            }
            index.incrementAndGet();
        }
    }

    private static class CheckUpdateFuture extends RetryFuture<TypedCheckResult> {

        private final String params;
        private String group;

        CheckUpdateFuture(QConfigHttpServerClient serverClient, String[] urls, Map<Meta, VersionProfile> files) {
            super(serverClient, urls);
            this.params = buildChangedListParams(files);
        }

        CheckUpdateFuture(String[] urls, String group) {
            super(urls);
            this.params = "";
            this.group = group;
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException {
            AsyncHttpClient.BoundRequestBuilder builder = HttpClientHolder.INSTANCE.preparePost(url);
            builder.addHeader(Constants.NEED_PURGE, Boolean.TRUE.toString());
            if (!isEmpty(group)) {
                builder.addQueryParam(Constants.GROUP_NAME, group);
            }
            builder.setBody(params);
            return builder;
        }

        private boolean isEmpty(String str) {
            return str == null || "".equals(str);
        }

        @Override
        protected boolean process(Response response) {
            if (response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
                set(TypedCheckResult.EMPTY);
                return true;
            }

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("response status code is {}", response.getStatusCode());
                return false;
            }

            try {
                set(parse(response));
            } catch (IOException e) {
                logger.warn("parse response error", e);
                return false;
            }
            return true;
        }

        protected TypedCheckResult parse(Response response) throws IOException {
            LineReader reader = new LineReader(new StringReader(response.getResponseBody(Constants.UTF_8.name())));
            Map<Meta, VersionProfile> result = new HashMap<Meta, VersionProfile>();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    append(result, line);
                }
            } catch (IOException e) {
                //ignore
            }


            if (Constants.PULL.equals(response.getHeader(Constants.UPDATE_TYPE))) {
                return new TypedCheckResult(result, TypedCheckResult.Type.PULL);
            } else {
                return new TypedCheckResult(result, TypedCheckResult.Type.UPDATE);
            }
        }

        private void append(Map<Meta, VersionProfile> result, String line) {
            try {
                Iterator<String> iterator = COMMA_SPLITTER.split(line).iterator();
                String groupName = iterator.next();
                String fileName = iterator.next();
                Long version = Long.parseLong(iterator.next());
                String profile = iterator.next();
                result.put(new Meta(groupName, fileName), new VersionProfile(version, profile));
            } catch (Exception e) {
                logger.error("配置中心返回的数据格式非法, {}", line, e);
            }
        }

        // 请求的格式如下所示:
        // qconfig_demo,mysql.properties,1
        // qconfig_demo,config.properties,2
        private String buildChangedListParams(Map<Meta, VersionProfile> files) {
            StringBuilder params = new StringBuilder(files.size() * 40);
            Iterator<Map.Entry<Meta, VersionProfile>> iterator = files.entrySet().iterator();
            if (iterator.hasNext()) {
                appendOneParam(params, iterator.next());
            }
            while (iterator.hasNext()) {
                params.append(Constants.LINE);
                appendOneParam(params, iterator.next());
            }
            return params.toString();
        }

        private void appendOneParam(StringBuilder params, Map.Entry<Meta, VersionProfile> entry) {
            Meta meta = entry.getKey();
            VersionProfile value = entry.getValue();
            params.append(meta.getGroupName()).append(',')
                    .append(meta.getFileName()).append(',')
                    .append(value.getVersion()).append(',')
                    .append(value.getProfile());
        }

    }

    private static class LongPollingCheckUpdateFuture extends CheckUpdateFuture {

        LongPollingCheckUpdateFuture(QConfigHttpServerClient serverClient, String[] urls, Map<Meta, VersionProfile> files) {
            super(serverClient, urls, files);
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException {
            AsyncHttpClient.BoundRequestBuilder builder = super.buildRequest(url);
            builder.setRequestTimeout(LONG_POLLING_TIMEOUT);
            return builder;
        }
    }

    private final static class RecordLoadingFuture extends RetryFuture<Void> {

        private final ConfigLogType type;

        private final Meta meta;

        private final long version;

        private final String remarks;

        private RecordLoadingFuture(String[] urls, ConfigLogType type, Meta meta, long version, String remarks) {
            super(urls, 0);
            this.type = type;
            this.meta = meta;
            this.version = version;
            this.remarks = remarks;
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) {
            return HttpClientHolder.INSTANCE.preparePost(url)
                    .addQueryParam(Constants.GROUP_NAME, meta.getGroupName())
                    .addQueryParam(Constants.DATAID_NAME, meta.getFileName())
                    .addQueryParam(Constants.VERSION_NAME, String.valueOf(version))
                    .addQueryParam(Constants.CONFIG_LOG_TYPE_CODE, String.valueOf(type.getCode()))
                    .addQueryParam(Constants.REMARKS_NAME, remarks);
        }

        @Override
        protected boolean process(Response response) {
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                set(null);
                return true;
            }
            logger.warn("response status code is {}", response.getStatusCode());
            return false;
        }
    }

    private static class LoadDataFuture<T> extends RetryFuture<T> {

        private final Meta meta;

        private final VersionProfile version;

        LoadDataFuture(QConfigHttpServerClient serverClient, String[] urls, Meta meta, VersionProfile version) {
            super(serverClient, urls);
            this.meta = meta;
            this.version = version;
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException {
            return HttpClientHolder.INSTANCE.prepareGet(url)
                    .addQueryParam(Constants.GROUP_NAME, meta.getGroupName())
                    .addQueryParam(Constants.DATAID_NAME, meta.getFileName())
                    .addQueryParam(Constants.VERSION_NAME, String.valueOf(version.getVersion()))
                    .addQueryParam(Constants.LOAD_PROFILE_NAME, version.getProfile());
        }

        @Override
        protected boolean process(Response response) {
            try {
                if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    setException(new ResultUnexpectedException(response.getStatusCode(), extractSubStatusCode(response),
                            "未找到所需的配置文件, file: " + meta.getFileName() + ", version: " + version));
                    return true;
                }

                if (response.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    String body = response.getResponseBody(Constants.UTF_8.name());
                    setException(new ResultUnexpectedException(response.getStatusCode(), extractSubStatusCode(response),
                            "服务端发生异常, " + body + ",file: " + meta.getFileName() + ", version: " + version));
                    return false;
                }

                if (response.getStatusCode() != HttpStatus.SC_OK) {
                    logger.warn("response status code is {}", response.getStatusCode());
                    return false;
                }

                String expected = response.getHeader(Constants.CHECKSUM_NAME);
                String body = response.getResponseBody(Constants.UTF_8.name());
                String actual = ChecksumAlgorithm.getChecksum(body);

                if (!actual.equals(expected)) {
                    logger.warn("checksum校验失败，重新尝试拉取配置. actual: {}, expected: {}", actual, expected);
                    return false;
                }
                return setResult(response, body);
            } catch (IOException e) {
                return false;
            }
        }

        private int extractSubStatusCode(Response response) {
            try {
                return Integer.parseInt(response.getHeader(Constants.SUB_CODE));
            } catch (NumberFormatException e) {
                return ResultUnexpectedException.NOT_SET;
            }
        }

        Snapshot<String> genSnapshot(VersionProfile versionProfile, String body, Response response) {
            String cacheControl = response.getHeader(Constants.CACHE_CONTROL);
            String contentEncoding = response.getHeader(Constants.CONTENT_ENCODING);
            FeatureRemote featureRemote = FeatureRemote.create()
                    .setIsLocalCache(!Constants.CACHE_NO_STORE.equals(cacheControl))
                    .build();
            return new Snapshot<String>(versionProfile, body, featureRemote);
        }

        protected boolean setResult(Response response, String body) {
            set((T) genSnapshot(version, body, response));
            return true;
        }
    }

    protected static class ForceReloadFuture extends LoadDataFuture<Snapshot<String>> {

        ForceReloadFuture(QConfigHttpServerClient serverClient, String[] urls, Meta key, long minVersion) {
            super(serverClient, urls, key, new VersionProfile(minVersion, VersionProfile.LOCAL_PROFILE));
        }

        @Override
        protected boolean setResult(Response response, String result) {
            String profile = response.getHeader(Constants.PROFILE_NAME);
            long version = Long.parseLong(response.getHeader(Constants.VERSION_NAME));
            set(genSnapshot(new VersionProfile(version, profile), result, response));
            return true;
        }
    }
}
