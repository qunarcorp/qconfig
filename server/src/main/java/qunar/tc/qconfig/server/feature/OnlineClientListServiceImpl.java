package qunar.tc.qconfig.server.feature;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by dongcao on 2018/9/27.
 */
@Service
public class OnlineClientListServiceImpl implements OnlineClientListService {

    private static final ConcurrentMap<ConfigMeta, Cache<String, Long>> onlineClients = Maps.newConcurrentMap();

    private static final long DEFAULT_TIMEOUT = 60 * 1000L;

    private volatile long timeout = DEFAULT_TIMEOUT;

    @PostConstruct
    private void init() {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                String newTimeout = conf.get("longPolling.server.timeout");
                if (!Strings.isNullOrEmpty(newTimeout)) {
                    timeout = Numbers.toLong(newTimeout, DEFAULT_TIMEOUT);
                }
            }
        });
    }

    @Override
    public void register(ConfigMeta meta, String ip, long version) {
        Cache<String, Long> cache = onlineClients.get(meta);
        if (cache== null) {
            Cache<String, Long> newCache = CacheBuilder
                    .newBuilder()
                    .expireAfterWrite(timeout, TimeUnit.MILLISECONDS)
                    .build();
            cache = onlineClients.putIfAbsent(meta, newCache);
            if (cache == null) {
                cache = newCache;
            }
        }

        cache.put(ip, version);
    }

    @Override
    public Set<String> getListeningClients(ConfigMeta meta) {
        Cache<String, Long> cache = onlineClients.get(meta);
        if (cache == null) {
            return ImmutableSet.of();
        }

        Set<String> clients = Sets.newHashSetWithExpectedSize((int) cache.size());
        for (String ip : cache.asMap().keySet()) {
            clients.add(ip);
        }

        return clients;
    }

    @Override
    public Set<ClientData> getListeningClientsData(ConfigMeta meta) {
        Cache<String, Long> cache = onlineClients.get(meta);
        if (cache == null) {
            return ImmutableSet.of();
        }

        Set<ClientData> clients = Sets.newHashSetWithExpectedSize((int)cache.size());
        for (Map.Entry<String, Long> entry : cache.asMap().entrySet()) {
            String ip = entry.getKey();
            long version = entry.getValue();
            ClientData client = new ClientData(ip, version);
            clients.add(client);
        }

        return clients;
    }
}
