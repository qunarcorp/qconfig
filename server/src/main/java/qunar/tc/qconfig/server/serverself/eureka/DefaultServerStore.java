package qunar.tc.qconfig.server.serverself.eureka;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.shared.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.bean.AppServerConfig;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.server.support.monitor.Monitor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2017 2017/8/10 16:25
 */
public class DefaultServerStore implements ServerStore {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServerStore.class);

    private final ServerDao serverDao;

    private final String defaultRoom;

    private final QConfigServer self;

    private final LoadingCache<String, String> ipRoomMapping;

    private volatile List<QConfigServer> servers = ImmutableList.of();

    public DefaultServerStore(final ServerDao serverDao, String currentRoom, int freshServerInfoIntervalMs) {
        logger.info("init qconfig server store");
        this.serverDao = serverDao;
        this.defaultRoom = currentRoom;

        AppServerConfig appServer = ServerManager.getInstance().getAppServerConfig();
        this.self = new QConfigServer(appServer.getIp(), appServer.getPort(), currentRoom);
        Preconditions.checkArgument(!"127.0.0.1".equals(self.getIp()), "self ip can not be 127.0.0.1");
        Preconditions.checkArgument(self.getPort() != 0, "self port can not be 0");
        serverDao.insert(self);
        logger.info("init qconfig server store successOf, self {}", self);

        this.ipRoomMapping = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
            @Override
            public String load(String ip) throws Exception {
                String room = serverDao.selectRoom(ip);
                if (Strings.isNullOrEmpty(room)) {
                    Monitor.serverIpRoomGetError.inc();
                    logger.error("can not find room for ip [{}]", ip);
                    throw new IllegalArgumentException("can not find room for ip [" + ip + "]");
                }
                return room;
            }
        });

        freshServerInfos();
        Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("fresh-qconfig-server-info")).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    freshServerInfos();
                } catch (Exception e) {
                    Monitor.serverInfoFreshError.inc();
                    logger.error("fresh qconfig server info error", e);
                }
            }
        }, freshServerInfoIntervalMs, freshServerInfoIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void freshServerInfos() {
        List<QConfigServer> newServers = doGetServers();
        if (newServers.isEmpty()) {
            return;
        }

        for (QConfigServer server : newServers) {
            this.ipRoomMapping.put(server.getIp(), server.getRoom());
        }
        this.servers = newServers;
    }

    @Override
    public QConfigServer self() {
        return self;
    }

    @Override
    public String defaultRoom() {
        return defaultRoom;
    }

    @Override
    public String getRoom(String ip) {
        return ipRoomMapping.getUnchecked(ip);
    }

    @Override
    public List<QConfigServer> getServers() {
        return servers;
    }

    @Override
    public List<QConfigServer> getAvailableServers() {
        DiscoveryClient client = DiscoveryManager.getInstance().getDiscoveryClient();
        Application application = client.getApplication("eureka");
        if (application == null) {
            logger.warn("eureka application is null");
            return emptyServers();
        }

        List<InstanceInfo> instances = application.getInstances();
        if (instances == null || instances.isEmpty()) {
            logger.warn("eureka instance is empty");
            return emptyServers();
        }

        List<QConfigServer> servers = Lists.newArrayListWithCapacity(instances.size());
        for (InstanceInfo instance : instances) {
            logger.debug("eureka qconfig server instance {}:{}", instance.getIPAddr(), instance.getPort());
            try {
                String ip = instance.getIPAddr();
                if ("127.0.0.1".equals(ip)) {
                    logger.warn("illegal qconfig server ip 127.0.0.1");
                    Monitor.serverLocalIpError.inc();
                    continue;
                }
                servers.add(new QConfigServer(ip, instance.getPort(), getRoom(ip)));
            } catch (Exception e) {
                // get room info error
                continue;
            }
        }

        if (servers.isEmpty()) {
            logger.warn("no legal eureka servers");
            return emptyServers();
        }
        return servers;
    }

    private List<QConfigServer> emptyServers() {
        logger.warn("can not find server instance");
        Monitor.noServerInstanceCounter.inc();
        return ImmutableList.of(self);
    }

    private List<QConfigServer> doGetServers() {
        return serverDao.selectServers();
    }
}
