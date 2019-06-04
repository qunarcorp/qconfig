package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.dao.FileDeleteDao;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.admin.service.ServerListService;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;
import qunar.tc.qconfig.servercommon.util.HttpClientHolder;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Future;

/**
 * User: zhaohuiyu
 * Date: 5/23/14
 * Time: 4:22 PM
 */
@Service
public class NotifyServiceImpl implements NotifyService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(NotifyServiceImpl.class);

    @Value("${notify.url}")
    private String notifyUrl;

    @Value("${notifyPush.url}")
    private String notifyPushUrl;

    @Value("${notifyIpPush.url}")
    private String notifyIpPushUrl;

    @Value("${notifyReference.url}")
    private String notifyReferenceUrl;

    @Value("${notifyPublic.url}")
    private String notifyPublicUrl;

    @Value("${notifyFixedVersion.url}")
    private String notifyFixedVersionUrl;

    private AsyncHttpClient httpClient;

    @Resource
    private FileDeleteDao fileDeleteDao;

    @Resource
    private ServerListService serverListService;

    @Override
    public void notify(final String group, final String dataId, final String profile) {
        List<String> urls = getServerUrls();
        if (urls == null || urls.isEmpty()) {
            return;
        }

        String uri = this.notifyUrl;
        logger.info("notify server, group: {}, data id: {}, profile: {}, uri: {}, servers: {}", group, dataId, profile, uri, urls);
        doNotify(urls, uri, "update", new Function<String, Request>() {
            @Override
            public Request apply(String url) {
                return getRequest(url, group, dataId, profile);
            }
        });
    }

    private List<String> getServerUrls() {
        return serverListService.getOnlineServerHosts();
    }

    private void doNotify(List<String> serverUrls, String uri, String type, Function<String, Request> requestBuilder) {
        List<ListenableFuture<Response>> futures = Lists.newArrayListWithCapacity(serverUrls.size());
        for (String oneServer : serverUrls) {
            String url = "http://" + oneServer + "/" + uri;
            Request request = requestBuilder.apply(url);
            ListenableFuture<Response> future = HttpListenableFuture.wrap(httpClient.executeRequest(request));
            futures.add(future);
        }

        dealResult(futures, serverUrls, type);
    }

    @Override
    public void notifyPush(final ConfigMeta meta, final long version, List<PushItemWithHostName> destinations) {
        List<String> serverUrls = getServerUrls();
        if (serverUrls.isEmpty()) {
            logger.warn("notify push server, {}, version: {}, but no server, {}", meta, version, destinations);
            return;
        }

        String uri = this.notifyPushUrl;
        logger.info("notify push server, {}, version: {}, uri: {}, servers: {}, {}", meta, version, uri, serverUrls, destinations);
        StringBuilder sb = new StringBuilder();
        for (PushItemWithHostName item : destinations) {
            sb.append(item.getHostname()).append(',')
                    .append(item.getIp()).append(',')
                    .append(item.getPort()).append(Constants.LINE);
        }
        final String destinationsStr = sb.toString();
        doNotify(serverUrls, uri, "push", new Function<String, Request>() {
            @Override
            public Request apply(String url) {
                AsyncHttpClient.BoundRequestBuilder builder = getBoundRequestBuilder(url, meta, version, destinationsStr);
                return builder.build();
            }
        });
    }

    private AsyncHttpClient.BoundRequestBuilder getBoundRequestBuilder(String url, ConfigMeta meta, long version, String destinationsStr) {
        AsyncHttpClient.BoundRequestBuilder builder = httpClient.preparePost(url);

        builder.addQueryParam(Constants.GROUP_NAME, meta.getGroup())
                .addQueryParam(Constants.DATAID_NAME, meta.getDataId())
                .addQueryParam(Constants.PROFILE_NAME, meta.getProfile())
                .addQueryParam(Constants.VERSION_NAME, String.valueOf(version))
                .addHeader(Constants.TOKEN_NAME, ServerManager.getInstance().getAppServerConfig().getToken());

        builder.setBody(destinationsStr);
        return builder;
    }


    @Override
    public void notifyPushIp(final ConfigMeta meta, final long version, List<Host> destinations) {
        List<String> serverUrls = getServerUrls();
        if (serverUrls.isEmpty()) {
            logger.warn("notify push server, {}, version: {}, but no server, {}", meta, version, destinations);
            return;
        }

        String uri = this.notifyIpPushUrl;
        logger.info("notify push server, {}, version: {}, uri: {}, servers: {}, {}", meta, version, uri, serverUrls, destinations);
        StringBuilder sb = new StringBuilder();
        for (Host item : destinations) {
            sb.append(item.getIp()).append(Constants.LINE);
        }
        final String destinationsStr = sb.toString();

        doNotify(serverUrls, uri, "admin/notifyIpPush", new Function<String, Request>() {
            @Override
            public Request apply(String url) {
                AsyncHttpClient.BoundRequestBuilder builder = getBoundRequestBuilder(url, meta, version, destinationsStr);
                return builder.build();
            }
        });
    }


    @Override
    public void notifyReference(final Reference reference, final RefChangeType changeType) {
        List<String> urls = getServerUrls();
        if (urls.isEmpty()) {
            return;
        }

        String uri = this.notifyReferenceUrl;
        logger.info("notify ref change server, change type: {}, {}, uri: {}, servers: {}", changeType, reference, uri, urls);
        doNotify(urls, uri, "reference", new Function<String, Request>() {
            @Override
            public Request apply(String url) {
                return getRequest(url, reference, changeType);
            }
        });
    }

    @Override
    public void notifyPublic(final ConfigMetaWithoutProfile meta) {
        List<String> urls = getServerUrls();
        if (urls == null || urls.isEmpty()) {
            return;
        }

        String uri = this.notifyPublicUrl;
        logger.info("notify public status, {}, uri: {}, servers: {}", meta, uri, urls);
        doNotify(urls, uri, "public", new Function<String, Request>() {
            @Override
            public Request apply(String url) {
                return getRequest(url, meta.getGroup(), meta.getDataId(), "");
            }
        });
    }

    @Override
    public void notifyAdminDelete(ConfigMeta configMeta) {
        List<String> envIps = serverListService.getServers();
        if (envIps.isEmpty()) {
            logger.error("no server ips");
            return;
        }

        fileDeleteDao.insert(configMeta, envIps);
    }

    @Override
    public void notifyFixedVersion(final ConfigMeta meta, final String ip, final long version) {
        List<String> serverUrls = getServerUrls();
        if (CollectionUtils.isEmpty(serverUrls)) {
            logger.warn("notify fixedVersionConsumer server, meta:{}, ip:{}, version: {}, but no server", meta, ip, version);
            return;
        }
        String uri = this.notifyFixedVersionUrl;
        logger.info("notify fixedVersionConsumer server, meta:{}, ip:{}, version: {}, servers:{}", meta, ip, version, serverUrls);
        doNotify(serverUrls, uri, "fixedVersion", new Function<String, Request>() {
            @Override
            public Request apply(String url) {
                AsyncHttpClient.BoundRequestBuilder builder = httpClient.preparePost(url);
                builder.addQueryParam(Constants.GROUP_NAME, meta.getGroup())
                        .addQueryParam(Constants.PROFILE_NAME, meta.getProfile())
                        .addQueryParam(Constants.DATAID_NAME, meta.getDataId())
                        .addQueryParam(Constants.VERSION_NAME, String.valueOf(version))
                        .addQueryParam("ip", ip)
                        .addHeader(Constants.TOKEN_NAME, ServerManager.getInstance().getAppServerConfig().getToken());
                return builder.build();
            }
        });
    }

    private void dealResult(List<ListenableFuture<Response>> futures, final List<String> urls, final String type) {
        final ListenableFuture<List<Response>> future = Futures.successfulAsList(futures);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                List<Response> list = getUnchecked(future);
                if (list == null) {
                    logger.error("{} notify server error", type);
                    Monitor.notifyServerFailCounter.inc(urls.size());
                    return;
                }
                for (int i = 0; i < list.size(); ++i) {
                    Response response = list.get(i);
                    if (response == null || response.getStatusCode() != HttpStatus.SC_OK) {
                        int code = response == null ? 0 : response.getStatusCode();
                        logger.error("{} notify server failed, code {}, {}", type, code, urls.get(i));
                        Monitor.notifyServerFailCounter.inc();
                    }
                }
            }
        }, Constants.CURRENT_EXECUTOR);
    }

    private Request getRequest(String url, Reference reference, RefChangeType changeType) {
        AsyncHttpClient.BoundRequestBuilder builder = httpClient.preparePost(url);

        builder.addFormParam(Constants.GROUP_NAME, reference.getGroup())
                .addFormParam(Constants.DATAID_NAME, reference.getAlias())
                .addFormParam(Constants.PROFILE_NAME, reference.getProfile())
                .addFormParam(Constants.REF_GROUP_NAME, reference.getRefGroup())
                .addFormParam(Constants.REF_DATAID_NAME, reference.getRefDataId())
                .addFormParam(Constants.REF_PROFILE, reference.getRefProfile())
                .addFormParam(Constants.REF_CHANGE_TYPE, changeType.text());

        return builder.build();
    }

    private Request getRequest(String url, String group, String dataId, String profile) {
        AsyncHttpClient.BoundRequestBuilder builder = httpClient.preparePost(url);
        builder.addFormParam(Constants.GROUP_NAME, group)
                .addFormParam(Constants.DATAID_NAME, dataId)
                .addFormParam(Constants.PROFILE_NAME, profile);
        return builder.build();
    }

    @Override
    public void afterPropertiesSet() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(notifyUrl), "notify url can not be empty");
        httpClient = HttpClientHolder.getHttpClient();
    }

    private <T> T getUnchecked(Future<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }
}
