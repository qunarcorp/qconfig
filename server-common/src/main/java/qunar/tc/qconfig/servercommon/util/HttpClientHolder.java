package qunar.tc.qconfig.servercommon.util;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * @author zhenyu.nie created on 2014 2014/6/20 9:48
 */
public class HttpClientHolder {

    private static final int CONN_TIMEOUT = 2000;

    private static final int READ_TIMEOUT = 3000;

    private static final AsyncHttpClient httpClient;

    static {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setConnectTimeout(CONN_TIMEOUT);
        builder.setRequestTimeout(READ_TIMEOUT);
        httpClient = new AsyncHttpClient(builder.build());
    }

    public static AsyncHttpClient getHttpClient() {
        return httpClient;
    }
}
