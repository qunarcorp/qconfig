package qunar.tc.qconfig.server.serverself.serverinfo;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.spring.QMapConfig;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.server.serverself.eureka.QConfigServer;
import qunar.tc.qconfig.server.serverself.eureka.ServerStore;
import qunar.tc.qconfig.server.support.context.ClientInfoService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: zhaohuiyu Date: 5/19/14 Time: 10:49 AM
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);

    @Resource
    private ServerStore serverStore;

    @Resource
    private ClientInfoService clientInfoService;

    private static final Splitter COMMA_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    private static volatile Set<String> testEnvIpSet = ImmutableSet.of();

    private static volatile Set<String> testRooms = ImmutableSet.of();

    @PostConstruct
    private void init() {
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                String testRoomsStr = conf.get("server.test.rooms");
                if (!Strings.isNullOrEmpty(testRoomsStr)) {
                    testRooms = ImmutableSet.copyOf(COMMA_SPLITTER.split(testRoomsStr));
                } else {
                    testRooms = ImmutableSet.of();
                }
            }
        });
    }

    @Override
    public Set<QConfigServer> list(final String room) {
        CurrentServers servers = initCurrentServer();
        return selectServer(room, servers);
    }

    @Override
    public Set<QConfigServer> all(final ClusterType type) {
        return all();
    }

    private Set<QConfigServer> selectServer(String room, CurrentServers servers) {
        String tokenEnv = clientInfoService.getEnv();
        if (Strings.isNullOrEmpty(room) && Strings.isNullOrEmpty(tokenEnv)) {
            return defaultServer();
        } else if (useProdServer(room, tokenEnv)) {
            return serversInPriority(room, getProdServers(servers));
        } else {
            return ImmutableSet.copyOf(getTestServer(servers).values());
        }
    }

    private SetMultimap<String, QConfigServer> getTestServer(CurrentServers servers) {
        return servers.getTestServers();
    }

    private SetMultimap<String, QConfigServer> getProdServers(CurrentServers servers) {
        return servers.getProdServers();
    }

    private CurrentServers initCurrentServer() {
        List<QConfigServer> availableServers = serverStore.getAvailableServers();
        SetMultimap<String, QConfigServer> roomProServerMap = HashMultimap.create();
        SetMultimap<String, QConfigServer> roomTestServerMap = HashMultimap.create();
        for (QConfigServer server : availableServers) {
            if (testEnvIpSet.contains(server.getIp())) {
                roomTestServerMap.put(server.getRoom(), server);
            } else {
                roomProServerMap.put(server.getRoom(), server);
            }
        }
        return new CurrentServers(roomProServerMap, roomTestServerMap);
    }

    private boolean useProdServer(String room, String tokenEnv) {
        return isProdRoom(room) || isProdRequest(tokenEnv);
    }

    private static boolean isProdRoom(String room) {

        if (testRooms.isEmpty()) {
            return false;
        }

        return !isTestRoom(room);
    }

    private static boolean isTestRoom(String room) {
        return Strings.isNullOrEmpty(room) || testRooms.contains(room);
    }

    private boolean isProdRequest(String tokenEnv) {
        return !Strings.isNullOrEmpty(tokenEnv) && Environment.fromEnvName(tokenEnv).isProd();
    }

    @Override
    public Set<QConfigServer> all() {
        List<QConfigServer> availableServers = serverStore.getAvailableServers();
        return ImmutableSet.copyOf(availableServers);
    }

    private Set<QConfigServer> serversInPriority(String room, SetMultimap<String, QConfigServer> roomServerMap) {
        if (roomServerMap.containsKey(room)) {
            return roomServerMap.get(room);
        } else if (roomServerMap.containsKey(serverStore.defaultRoom())) {
            return roomServerMap.get(serverStore.defaultRoom());
        } else {
            return ImmutableSet.copyOf(roomServerMap.asMap().entrySet().iterator().next().getValue());
        }
    }


    @Override
    public Set<QConfigServer> defaultServer() {
        CurrentServers servers = initCurrentServer();
        return serversInPriority(serverStore.defaultRoom(), servers.getProdServers());
    }

    private static class CurrentServers {

        private final SetMultimap<String, QConfigServer> prodServers;

        private final SetMultimap<String, QConfigServer> testServers;

        private CurrentServers(SetMultimap<String, QConfigServer> prodServers, SetMultimap<String, QConfigServer> testServers) {
            this.prodServers = prodServers;
            this.testServers = testServers;
        }

        public SetMultimap<String, QConfigServer> getProdServers() {
            return prodServers;
        }

        public SetMultimap<String, QConfigServer> getTestServers() {
            return testServers;
        }
    }
}
