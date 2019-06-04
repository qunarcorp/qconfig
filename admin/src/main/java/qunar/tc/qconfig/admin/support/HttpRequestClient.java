package qunar.tc.qconfig.admin.support;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.Constants;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class HttpRequestClient {

    private final Logger logger = LoggerFactory.getLogger(HttpRequestClient.class);

    private static final String DEFAULT_CHARSET = Charsets.UTF_8.name();

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int TIMEOUT_MS = 3000;

    @Resource
    private AsyncHttpClient httpClient;

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    public <R> JsonV2<R> get(String url, Map<String, String> params, TypeReference<JsonV2<R>> typeReference) {
        AsyncHttpClient.BoundRequestBuilder prepareGet = httpClient.prepareGet(url);
        prepareGet.setHeader("Accept", "application/json");
        prepareGet.setHeader("Content-Type", "application/json; charset=utf-8");
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                prepareGet.addQueryParam(param.getKey(), param.getValue());
            }
        }
        Request request = prepareGet.build();
        try {
            Response response = httpClient.executeRequest(request).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            int statusCode = response.getStatusCode();
            if (statusCode != Constants.OK_STATUS) {
                throw new RuntimeException(String.format("http request error,url:%s,status code:%d",url,statusCode));
            }
            return mapper.readValue(response.getResponseBody(DEFAULT_CHARSET), typeReference);
        }  catch (Exception e) {
            logger.error("request failOf, url [{}], params {}", url, params, e);
            throw new RuntimeException(e);
        }
    }

}
