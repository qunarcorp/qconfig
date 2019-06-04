package qunar.tc.qconfig.client.impl;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.LineReader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.QConfigAttributes;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaohui.yu
 * 1/22/18
 */
class QConfigEntryPoint {

    protected static final Logger logger = LoggerFactory.getLogger("qunar.tc.qconfig.client.impl.HttpClient");

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();

    static final String CHECK_UPDATE_URL = "/client/checkupdatev2";

    static final String LONG_POLLING_CHECK_UPDATE_URL = "/client/longPollingCheckupdatev2";

    static final String LOAD_DATA_URL = "/client/getconfigv2";

    static final String FORCE_RELOAD_URL = "/client/forceloadv2";

    static final String RECORD_LOADING_URL = "/client/recordloadingv2";

    static final String GET_GROUP_FILES = "/client/getgroupfiles";

    private static final int DEFAULT_TIMEOUT = 1000;

    private static final QConfigAttributes attrs = QConfigAttributesLoader.getInstance();

    private static final String ENTRY_POINT_URL_V2 = "http://" + attrs.getServerUrl() + "/entrypointv2";


    private static final String[][] DEFAULT_ADDRESSES = new String[][]{attrs.getDefaultAddresses(), attrs.getDefaultHttpsAddresses()};

    private static final Map<String, String[]> httpUrls = new ConcurrentHashMap<String, String[]>();

    private static final Map<String, String[]> httpsUrls = new ConcurrentHashMap<String, String[]>();

    private ListenableFuture<String[][]> urlsFuture;

    QConfigEntryPoint() {
        refresh();
    }

    String[] resolveHttpUrl(String suffix, boolean isHttps) {
        String[] urls = isHttps ? httpsUrls.get(suffix) : httpUrls.get(suffix);
        if (urls != null) {
            return urls;
        }

        String[][] urlsFutureArray;
        try {
            urlsFutureArray = urlsFuture.get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (urlsFutureArray == null) {
                urlsFutureArray = DEFAULT_ADDRESSES;
            }
        } catch (Exception e) {
            logger.info("从entrypointv2接口获取http链接失败！", e);
            urlsFutureArray = DEFAULT_ADDRESSES;
        }
        return genUrls(suffix, urlsFutureArray, isHttps);
    }

    private static final int URLS_ARRAY_LENGTH = 2;

    /**
     * 定期的是去获取最新的server list
     */
    public void refresh() {
        try {
            final ListenableFuture<String[][]> future = listAllServerUrls();
            urlsFuture = future;
            future.addListener(new Runnable() {
                @Override
                public void run() {
                    updateUrl(future, CHECK_UPDATE_URL, false);
                    updateUrl(future, LONG_POLLING_CHECK_UPDATE_URL, false);

                    updateUrl(future, LOAD_DATA_URL, false);
                    updateUrl(future, FORCE_RELOAD_URL, false);
                    updateUrl(future, LOAD_DATA_URL, true);
                    updateUrl(future, FORCE_RELOAD_URL, true);

                    updateUrl(future, RECORD_LOADING_URL, false);
                    updateUrl(future, GET_GROUP_FILES, false);
                }
            }, Constants.CURRENT_EXECUTOR);
        } catch (Exception e) {
            logger.debug("refresh failed", e);
        }
    }

    private void updateUrl(ListenableFuture<String[][]> future, String suffix, boolean isHttps) {
        String[][] addresses;
        try {
            addresses = future.get();
            if (addresses == null) return;
        } catch (Exception e) {
            logger.info("从entrypointv2接口获取http链接失败！", e);
            return;
        }

        String[] urls = genUrls(suffix, addresses, isHttps);
        if (isHttps) {
            httpsUrls.put(suffix, urls);
        } else {
            httpUrls.put(suffix, urls);
        }
    }

    private ListenableFuture<String[][]> listAllServerUrls() {
        final String systemHttpServerUrls = System.getProperty("qserver.http.urls");
        final String systemHttpsServiceUrls = System.getProperty("qserver.https.urls");

        AsyncHttpClient.BoundRequestBuilder builder = HttpClientHolder.INSTANCE.prepareGet(ENTRY_POINT_URL_V2);
        builder.addHeader(Constants.TOKEN_NAME, Tokens.getToken());
        HttpListenableFuture<Response> future = HttpListenableFuture.wrap(HttpClientHolder.INSTANCE.executeRequest(builder.build()));
        return Futures.transform(future, new Function<Response, String[][]>() {
            @Override
            public String[][] apply(Response input) {
                try {
                    if (input.getStatusCode() != HttpStatus.SC_OK) {
                        logger.info("从entrypoint2获取serverlist失败, code [{}]", input.getStatusCode());
                        return null;
                    }

                    LineReader reader = new LineReader(new StringReader(input.getResponseBody(Constants.UTF_8.name())));
                    List<String> urls = Lists.newArrayList();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            urls.add(line);
                        }
                    } catch (IOException e) {
                        //ignore
                    }
                    String httpServerUrlStr;
                    String httpsServerUrlStr;

                    if (!Strings.isNullOrEmpty(systemHttpServerUrls) && !Strings.isNullOrEmpty(systemHttpsServiceUrls)) {
                        httpServerUrlStr = systemHttpServerUrls;
                        httpsServerUrlStr = systemHttpsServiceUrls;
                    } else {
                        if (urls.size() > 1) {
                            httpServerUrlStr = urls.get(0);
                            httpsServerUrlStr = urls.get(1);
                        } else {
                            logger.warn("no qconfig server address find entrypoint2");
                            return null;
                        }
                    }

                    List<String> httpAddresses = Lists.newArrayList(COMMA_SPLITTER.split(httpServerUrlStr));
                    List<String> httpsAddresses = Lists.newArrayList(COMMA_SPLITTER.split(httpsServerUrlStr));
                    String[][] result = new String[URLS_ARRAY_LENGTH][];
                    result[0] = httpAddresses.toArray(new String[httpAddresses.size()]);
                    result[1] = httpsAddresses.toArray(new String[httpsAddresses.size()]);
                    return result;
                } catch (Exception e) {
                    logger.info("从entrypoint2获取serverlist失败", e);
                    return null;
                }
            }
        }, MoreExecutors.directExecutor());
    }

    private String[] genUrls(String suffix, String[][] addresses, boolean isHttps) {
        String[] address = isHttps ? addresses[1] : addresses[0];
        String[] urls = new String[address.length];
        for (int i = 0; i < address.length; ++i) {
            if (isHttps) {
                urls[i] = "https://" + address[i] + suffix;
            } else {
                urls[i] = "http://" + address[i] + suffix;
            }
        }
        return urls;
    }

}