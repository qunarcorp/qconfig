package qunar.tc.qconfig.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.multipart.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.UploadResult;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.*;

import java.io.IOException;

/**
 * Created by zhaohui.yu
 * 11/8/17
 */
class QConfigAdminClient {
    private static final Logger logger = LoggerFactory.getLogger("qunar.tc.qconfig.client.impl.HttpClient");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final QConfigAttributes attrs = QConfigAttributesLoader.getInstance();

    private static final String UPLOAD_URL = "http://" + attrs.getAdminUrl() + "/api/config/upload";

    private static final String LOAD_LATEST_SNAPSHOT = "http://" + attrs.getAdminUrl() + "/api/config/latestsnapshot";

    private static final String CHANGE_STATUS_URL = "http://" + attrs.getAdminUrl() + "/api/config/status";//客户端审核发布配置接口

    private static QConfigAdminClient INSTANCE = new QConfigAdminClient();

    public static QConfigAdminClient getInstance() {
        return INSTANCE;
    }

    public ListenableFuture<UploadResult> upload(Meta meta, VersionProfile versionProfile, String data, boolean isPublic, String operator, boolean directPublish, String description) {
        return new UploadFuture(UPLOAD_URL, meta, versionProfile, data, isPublic, operator, directPublish, description).request();
    }

    ListenableFuture<UploadResult> changeStatus(String group, String dataId, String profile, long version, String operator, StatusType statusType, boolean isPublic) {
        return new ChangeStatusFuture(CHANGE_STATUS_URL, new Meta(group, dataId), profile, version, operator, statusType, isPublic).request();
    }

    ListenableFuture<Snapshot<String>> loadCandidateSnapShotData(String group, String dataId) {
        return new LoadCandidateSnapShotData(LOAD_LATEST_SNAPSHOT, group, dataId).request();
    }

    private static class LoadCandidateSnapShotData extends SimpleFuture<Snapshot<String>> {

        private String group;

        private String dataId;

        private final String API_V = "v1";

        LoadCandidateSnapShotData(String url, String group, String dataId) {
            super(url);
            this.group = group;
            this.dataId = dataId;
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException {
            AsyncHttpClient.BoundRequestBuilder builder = HttpClientHolder.INSTANCE.prepareGet(url);
            builder.addQueryParam(Constants.TOKEN_NAME, token)
                    .addQueryParam(Constants.GROUP_NAME, group)
                    .addQueryParam(Constants.DATAID_NAME, dataId)
                    .addQueryParam(Constants.BUILD_GROUP, EnvironmentAware.determinedEnv())
                    .addQueryParam(Constants.API_VERSION, API_V);
            return builder;
        }

        @Override
        protected Snapshot<String> parse(Response response) throws Exception {
            int code = Integer.parseInt(response.getHeader(Constants.CODE));
            logger.debug("response code is {}", code);
            String message = response.getResponseBody(Constants.UTF_8.name());
            logger.debug("response message is {}", message);
            if (code == ApiResponseCode.OK_CODE) {
                JsonNode jsonNode = objectMapper.readTree(message);
                JsonNode profileNode = jsonNode.get("profile");
                JsonNode versionNode = jsonNode.get("version");
                JsonNode contentNode = jsonNode.get("content");
                JsonNode statusNode = jsonNode.get("statuscode");
                if (profileNode != null
                        && versionNode != null
                        && contentNode != null
                        && statusNode != null) {
                    return new Snapshot<String>(profileNode.asText(), versionNode.asLong(), contentNode.asText(), StatusType.codeOf(statusNode.asInt()));
                }
            }
            return null;
        }
    }

    private static class ChangeStatusFuture extends SimpleFuture<UploadResult> {

        private final Meta meta;

        private final long basedVersion;

        private final String fileProfile;

        private final String API_V = "v1";

        private final String operator;

        private final StatusType statusType;

        private final boolean isPublic;

        ChangeStatusFuture(String url, Meta meta, String fileProfile, long basedVersion, String operator, StatusType statusType, boolean isPublic) {
            super(url);
            this.meta = meta;
            this.basedVersion = basedVersion;
            this.fileProfile = fileProfile;
            this.operator = operator;
            this.statusType = statusType;
            this.isPublic = isPublic;
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException {
            AsyncHttpClient.BoundRequestBuilder builder = HttpClientHolder.INSTANCE.preparePost(url);
            builder.addQueryParam(Constants.TOKEN_NAME, token)
                    .addQueryParam(Constants.GROUP_NAME, meta.getGroupName())
                    .addQueryParam(Constants.DATAID_NAME, meta.getFileName())
                    .addQueryParam(Constants.EDIT_VERSION, String.valueOf(basedVersion))
                    .addQueryParam(Constants.BUILD_GROUP, EnvironmentAware.determinedEnv())
                    .addQueryParam(Constants.FILE_PROFILE, fileProfile)
                    .addQueryParam(Constants.API_VERSION, API_V)
                    .addQueryParam(Constants.OPERATOR, operator)
                    .addQueryParam(Constants.STATUS_CODE, String.valueOf(statusType.code()))
                    .addQueryParam(Constants.ISPUBLIC, String.valueOf(isPublic));
            return builder;
        }

        @Override
        protected UploadResult parse(Response response) throws Exception {
            return parseUploadResult(response);
        }
    }

    private static UploadResult parseUploadResult(Response response) throws IOException {
        int code = Integer.parseInt(response.getHeader(Constants.CODE));
        logger.debug("response code is {}", code);
        String message = response.getResponseBody(Constants.UTF_8.name());
        logger.debug("response message is {}", message);
        return new UploadResult(code, message);
    }

    private static class UploadFuture extends SimpleFuture<UploadResult> {

        private final Meta meta;

        private final VersionProfile versionProfile;

        private final String data;

        private boolean isPublic;

        private static final String API_V = "1";

        private final String operator;

        private final boolean directPublish;

        private final String description;

        UploadFuture(String url,
                     Meta meta,
                     VersionProfile versionProfile,
                     String data,
                     boolean isPublic,
                     String operator,
                     boolean directPublish,
                     String description) {
            super(url);
            this.meta = meta;
            this.versionProfile = versionProfile;
            this.data = data;
            this.isPublic = isPublic;
            this.operator = operator;
            this.directPublish = directPublish;
            this.description = description;
        }

        @Override
        protected AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException {
            AsyncHttpClient.BoundRequestBuilder builder = HttpClientHolder.INSTANCE.preparePost(url);
            builder.addQueryParam(Constants.TOKEN_NAME, token)
                    .addQueryParam(Constants.GROUP_NAME, meta.getGroupName())
                    .addQueryParam(Constants.DATAID_NAME, meta.getFileName())
                    .addQueryParam(Constants.VERSION_NAME, String.valueOf(versionProfile.getVersion()))
                    .addQueryParam(Constants.BUILD_GROUP, EnvironmentAware.determinedEnv())
                    .addQueryParam(Constants.FILE_PROFILE, versionProfile.getProfile())
                    .addQueryParam(Constants.API_VERSION, API_V)
                    .addQueryParam(Constants.ISPUBLIC, String.valueOf(isPublic))
                    .addQueryParam(Constants.OPERATOR, operator)
                    .addQueryParam(Constants.ISDIRECTPUBLISH, String.valueOf(directPublish))
                    .addQueryParam(Constants.DESCRIPTION, description)
                    .addBodyPart(new StringPart(Constants.CONTENT, data, "text/html", Charsets.UTF_8));
            return builder;
        }

        @Override
        protected UploadResult parse(Response response) throws IOException {
            return parseUploadResult(response);
        }
    }

    private abstract static class SimpleFuture<T> extends AbstractFuture<T> {

        protected String url;

        protected static final String token;

        static {
            token = Tokens.getToken();
        }

        SimpleFuture(String url) {
            this.url = url;

        }

        protected abstract AsyncHttpClient.BoundRequestBuilder buildRequest(String url) throws IOException;

        public SimpleFuture<T> request() {
            if (isDone()) {
                return this;
            }

            try {
                if (Strings.isNullOrEmpty(token)) {
                    String message = "无法获取应用中心下发的验证信息";
                    logger.error(message, new RuntimeException(message));
                    setException(new RuntimeException(message));
                    return this;
                }
                AsyncHttpClient.BoundRequestBuilder builder = buildRequest(url);
                Request request = builder.build();
                final com.ning.http.client.ListenableFuture<Response> future = HttpClientHolder.INSTANCE.executeRequest(request);

                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        Response response = null;
                        try {
                            response = future.get();
                        } catch (Exception e) {
                            logger.error("get response error", e);
                            setException(e);
                            return;
                        }
                        process(response);
                    }
                }, Constants.CURRENT_EXECUTOR);
            } catch (Exception e) {
                logger.error("发送请求失败 error", e);
                setException(e);
            }
            return this;
        }

        protected boolean process(Response response) {
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                String message = "请求发生异常, code: " + response.getStatusCode();
                logger.error(message, new RuntimeException(message));
                setException(new RuntimeException(message));
                return true;
            }

            try {
                set(parse(response));
            } catch (Exception e) {
                logger.error("解析响应内容失败", e);
                setException(e);
            }
            return false;
        }

        protected abstract T parse(Response response) throws Exception;
    }
}
