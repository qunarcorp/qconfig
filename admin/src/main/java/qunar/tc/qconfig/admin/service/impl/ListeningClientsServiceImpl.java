package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.ServerListService;
import qunar.tc.qconfig.admin.support.HostnameUtil;
import qunar.tc.qconfig.client.impl.HttpClientHolder;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.common.support.json.JsonMapper;
import qunar.tc.qconfig.common.support.json.MapperBuilder;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Numbers;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author zhenyu.nie created on 2018 2018/5/18 16:57
 */
@Service
public class ListeningClientsServiceImpl implements ListeningClientsService {

    private static final Logger logger = LoggerFactory.getLogger(ListeningClientsServiceImpl.class);

    private static final ListeningScheduledExecutorService executor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));

    private static final JsonMapper jsonMapper = MapperBuilder.getDefaultMapper();

    @Resource
    private ServerListService serverListService;

    @Resource
    private FixedConsumerVersionService fixedConsumerVersionService;

    @QConfig("config.properties")
    private Map<String, String> config;

    private AsyncHttpClient httpClient = HttpClientHolder.INSTANCE;

    @Override
    public Map<String, Set<ClientData>> getListeningPushStatus(ConfigMeta configMeta, long editVersion, List<String> ipPorts) throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, Set<ClientData>> result = Maps.newHashMapWithExpectedSize(3);
        Set<ClientData> successClientDataSet = Sets.newHashSet();
        Set<ClientData> failureClientDataSet = Sets.newHashSet();
        Set<ClientData> fixedVersionClientDataSet = Sets.newHashSet();
        Set<ClientData> offlineClientDataSet = Sets.newHashSet();

        result.put("success", successClientDataSet);
        result.put("failure", failureClientDataSet);
        result.put("fixedversion", fixedVersionClientDataSet);
        result.put("offline", offlineClientDataSet);
        ListenableFuture<Set<ClientData>> data = getListeningClientsData(configMeta, false);
        Set<ClientData> clientDataSet = data.get(Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (clientDataSet.isEmpty() || ipPorts.isEmpty()) {
            return result;
        }
        Map<String, ClientData> clientDataMap = Maps.newHashMapWithExpectedSize(clientDataSet.size());
        for (ClientData clientData : clientDataSet) {
            clientDataMap.put(clientData.ipPortStr(), clientData);
        }

        for (String ipport : ipPorts) {
            ClientData clientData = clientDataMap.get(ipport);
            if (clientData == null) {
                String[] ipPort = ipport.split(":");
                offlineClientDataSet.add(new ClientData(ipPort[0], Integer.valueOf(ipPort[1])));
                continue;
            }
            if (clientData.isFixed()) {
                fixedVersionClientDataSet.add(clientData);
                continue;
            }
            if (clientData.getVersion() == editVersion) {
                successClientDataSet.add(clientData);
            } else {
                failureClientDataSet.add(clientData);
            }
        }
        return result;
    }
    @Override
    public ListenableFuture<Set<Host>> getListeningClients(final ConfigMeta meta) {
        return Futures.transform(getListeningClientsData(meta, true), clientDataSet -> {
            Set<Host> allHosts = Sets.newHashSet();
            for (ClientData clientData : clientDataSet) {
                allHosts.add(new Host(clientData.getIp(), clientData.getHostname()));
            }
            return allHosts;
        }, executor);
    }

    @Override
    public ListenableFuture<Set<ClientData>> getListeningClientsData(final ConfigMeta meta, final boolean needRemoveFixed) {
        String listeningClientsUri = "/clients/v2/listening";
        List<String> serverUrls = serverListService.getOnlineServerHosts();
        ListenableFuture<Set<ClientData>> firstClientsDataFuture = doGetListeningClients(serverUrls, meta, listeningClientsUri, new ParseClientsDataFunction());
        ListenableFuture<Set<ClientData>> secondClientsDataFuture = getSecondClients(serverUrls, meta, listeningClientsUri, new ParseClientsDataFunction());
        ListenableFuture<List<Set<ClientData>>> allClientsDataFuture = Futures.allAsList(firstClientsDataFuture, secondClientsDataFuture);
        return Futures.transform(allClientsDataFuture, new Function<List<Set <ClientData>>, Set<ClientData>>() {
            @Override
            public Set<ClientData> apply(List<Set<ClientData>> allClients) {
                Set<ClientData> allClientData = Sets.newHashSet();
                for (Set<ClientData> clientData : allClients) {
                    allClientData.addAll(clientData);
                }
                fixAndFilterClientsData(allClientData, meta, needRemoveFixed);
                return allClientData;
            }
        }, executor);
    }

    @Override
    public Optional<ClientData> getListeningClientsData(final ConfigMeta meta, final String ip) throws InterruptedException, ExecutionException, TimeoutException {
        ListenableFuture<Set<ClientData>> clientsDataSet = getListeningClientsData(meta, false);
        Set<ClientData> clientDataSet = clientsDataSet.get(Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<ClientData> clientDataList = clientDataSet.stream().filter(input -> input != null && input.getIp().equalsIgnoreCase(ip)).collect(Collectors.toList());
        if (clientDataList.size() > 0) {
            return Optional.of(clientDataList.get(0));
        } else {
            return Optional.empty();
        }
    }

    private <T> ListenableFuture<Set<T>> getSecondClients(final List<String> serverUrls, final ConfigMeta meta, final String uri, final Function<String, Set<T>> parseFunction) {
        final SettableFuture<Set<T>> secondClientsFuture = SettableFuture.create();
        long delay = Numbers.toLong(config.get("queryListeningClients.delayMillis"), 1000);
        executor.schedule(() -> {
            ListenableFuture<Set<T>> secondFuture = doGetListeningClients(serverUrls, meta, uri, parseFunction);
            Futures.addCallback(secondFuture, new FutureCallback<Set<T>>() {
                @Override
                public void onSuccess(Set<T> result) {
                    secondClientsFuture.set(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    secondClientsFuture.setException(t);
                }
            }, Constants.CURRENT_EXECUTOR);
        }, delay, TimeUnit.MILLISECONDS);
        return secondClientsFuture;
    }

    private void fixAndFilterClientsData(Set<ClientData> clientDatas, ConfigMeta meta, boolean ignoreFixedVersionClient) {
        Set<String> fixedIps = fixedConsumerVersionService.findIpAndVersions(meta).keySet();
        if (!fixedIps.isEmpty()) {
            List<ClientData> ignores = Lists.newArrayList();
            for (ClientData clientData : clientDatas) {
                // 客户端版本被锁定
                if (fixedIps.contains(clientData.getIp())) {
                    if (ignoreFixedVersionClient) {
                        ignores.add(clientData);
                    } else {
                        clientData.setFixed(true);
                        clientData.setFixedVersion(clientData.getVersion());
                    }
                }
            }
            clientDatas.removeAll(ignores);
        }
    }

    private <T> ListenableFuture<Set<T>> doGetListeningClients(List<String> serverUrls, ConfigMeta meta, String uri, Function<String, Set<T>> function) {
        List<ListenableFuture<Response>> futures = Lists.newArrayListWithCapacity(serverUrls.size());
        for (String oneServer : serverUrls) {
            String url = "http://" + oneServer + uri;
            AsyncHttpClient.BoundRequestBuilder builder = httpClient.prepareGet(url);
            builder.addQueryParam(Constants.GROUP_NAME, meta.getGroup())
                    .addQueryParam(Constants.DATAID_NAME, meta.getDataId())
                    .addQueryParam(Constants.PROFILE_NAME, meta.getProfile());
            Request request = builder.build();
            ListenableFuture<Response> future = HttpListenableFuture.wrap(httpClient.executeRequest(request));
            futures.add(future);
        }

        return parseData(serverUrls, futures, function);
    }

    private <T> ListenableFuture<Set<T>> parseData(final List<String> serverUrls, final List<ListenableFuture<Response>> futures, final Function<String, Set<T>> parseFunction) {
        final SettableFuture<Set<T>> result = SettableFuture.create();
        ListenableFuture<List<Response>> responseFuture = Futures.successfulAsList(futures);
        Futures.addCallback(responseFuture, new FutureCallback<List<Response>>() {
            @Override
            public void onSuccess(List<Response> responses) {
                Set<T> allClients = Sets.newHashSet();
                for (int i = 0; i < responses.size(); ++i) {
                    Response response = responses.get(i);
                    if (response != null && response.getStatusCode() == HttpStatus.SC_OK) {
                        String responseStr = null;
                        try {
                            responseStr = response.getResponseBody("utf8");
                        } catch (IOException e) {
                            logger.warn("get listening client response from server error", e);
                        }
                        allClients.addAll(parseFunction.apply(responseStr));
                    } else {
                        logger.warn("get listening clients error with {}", serverUrls.get(i));
                    }
                }
                result.set(allClients);
            }

            @Override
            public void onFailure(Throwable t) {
                result.setException(t);
            }
        }, executor);

        return result;
    }

    private class ParseClientsDataFunction implements Function<String, Set<ClientData>> {

        @Override
        public Set<ClientData> apply(String input) {
            Set<ClientData> clientDataSet = jsonMapper.readValue(input, new TypeReference<Set<ClientData>>() {
            });
            for (ClientData clientData : clientDataSet) {
                clientData.setHostname(HostnameUtil.getHostnameFromIp(clientData.getIp()));
            }
            return clientDataSet;
        }
    }
}
