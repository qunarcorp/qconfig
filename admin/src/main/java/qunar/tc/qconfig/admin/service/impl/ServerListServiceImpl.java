package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ServerDao;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.service.ServerListService;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.impl.HttpClientHolder;
import qunar.tc.qconfig.client.spring.QMapConfig;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.Strings;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/5/18 17:15
 */
@Service
public class ServerListServiceImpl implements ServerListService {

    private static final Logger logger = LoggerFactory.getLogger(ServerListServiceImpl.class);

    private static final Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private static final String QCONFIG_SERVER_HOST_KEY = "qconfig.server.host";

    private static final String QCONFIG_ENTRYPOINT_BETA_URL_SWITCH_KEY = "admin.switch.entrypoint.beta.url";

    private String qConfigServerUrl;

    private boolean enableBetaEntrypointUrl = false;

    private static final String ENTRYPOINT_URL = "/entrypoint?type=all";

    private List<String> envServers = Lists.newCopyOnWriteArrayList();

    private List<String> envOnlineServers = Lists.newCopyOnWriteArrayList();

    private AsyncHttpClient httpClient;

    @Autowired
    private ServerDao serverDao;

    @PostConstruct
    public void init() {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                updateConfigs(conf);
            }
        });

        httpClient = HttpClientHolder.INSTANCE;
        updateServers();
        updateOnlineServers();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        startTasks(scheduledExecutorService);
    }

    @Override
    public boolean contains(String ip) {
        return envServers.contains(ip);
    }

    @Override
    public List<String> getServers() {
        return envServers;
    }

    @Override
    public List<String> getOnlineServerHosts() {
        return ImmutableList.copyOf(envOnlineServers);
    }

    private void updateConfigs(Map<String, String> configs) {
        String host = configs.get(QCONFIG_SERVER_HOST_KEY);
        qConfigServerUrl = genEntrypointUrl(host);
        enableBetaEntrypointUrl = Strings.getBoolean(configs.get(QCONFIG_ENTRYPOINT_BETA_URL_SWITCH_KEY), enableBetaEntrypointUrl);
    }


    private void updateOnlineServers() {
        AsyncHttpClient.BoundRequestBuilder builder = httpClient.prepareGet(qConfigServerUrl);
        ListenableFuture<Response> future = HttpListenableFuture.wrap(httpClient.executeRequest(builder.build()));

        try {
            Response response = future.get();
            if (response.getStatusCode() == HttpStatus.OK.value()) {
                Iterable<String> it = SPLITTER.split(response.getResponseBody());
                envOnlineServers = Lists.newArrayList(it);
            }

        } catch (Exception e) {
//            throw new RuntimeException("access /entrypoint?type=all failed", e);
        }
    }

    private void updateServers() {
        envServers = serverDao.getServers();
    }

    private String genEntrypointUrl(String host) {
        if (!host.startsWith("http://")) {
            host = "http://" + host;
        }
        host = host + ENTRYPOINT_URL;

        return host;
    }

    private void startTasks(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setName("update_qconfig_servers_from_db");
            try {
                updateServers();
            } catch (Throwable t) {
                logger.warn("update qconfig server list from db failed", t);
            }
        }, 5, 5, TimeUnit.MINUTES);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setName("update_online_qconfig_servers");
            try {
                updateOnlineServers();
            } catch (Throwable t) {
                logger.warn("update qconfig online server list failed", t);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

}
