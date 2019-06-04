package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.dao.PushConfigVersionDao;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PushConfigVersionItem;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 14:51
 */
@Service
public class CachePushConfigVersionServiceImpl implements CachePushConfigVersionService {

    private static final Logger logger = LoggerFactory.getLogger(CachePushConfigVersionServiceImpl.class);

    private volatile ConcurrentMap<Key, Version> cache = Maps.newConcurrentMap();

    @Resource
    private PushConfigVersionDao pushConfigVersionDao;

    @PostConstruct
    public void init() {
        freshPushVersionCache();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("fresh-push-version-thread");
                try {
                    freshPushVersionCache();
                } catch (Throwable e) {
                    logger.error("fresh push version error", e);
                }
            }
        }, 3, 3, TimeUnit.MINUTES);
    }

    private void freshPushVersionCache() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            logger.info("fresh push version cache");
            List<PushConfigVersionItem> pushItems = pushConfigVersionDao.select();

            ConcurrentMap<Key, Version> newCache = new ConcurrentHashMap<Key, Version>(pushItems.size());
            for (PushConfigVersionItem pushItem : pushItems) {
                newCache.put(new Key(pushItem.getMeta(), pushItem.getIp()), new Version(pushItem.getVersion()));
            }

            this.cache = newCache;
            logger.info("fresh push version cache successOf, count [{}]", pushItems.size());
        } finally {
            Monitor.freshPushVersionCache.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public Optional<Long> getVersion(ConfigMeta meta, String ip) {
        Version version = cache.get(new Key(meta, ip));
        return version != null ? Optional.of(version.get()) : Optional.<Long>absent();
    }

    @Override
    public void update(PushConfigVersionItem item) {
        Key key = new Key(item.getMeta(), item.getIp());
        long newVersion = item.getVersion();

        Version old = cache.putIfAbsent(key, new Version(newVersion));
        if (old != null) {
            old.update(newVersion);
        }
    }

    private static class Key {
        private final ConfigMeta meta;
        private final String ip;

        public Key(ConfigMeta meta, String ip) {
            this.meta = meta;
            this.ip = ip;
        }

        public ConfigMeta getMeta() {
            return meta;
        }

        public String getIp() {
            return ip;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (meta != null ? !meta.equals(key.meta) : key.meta != null) return false;
            return ip != null ? ip.equals(key.ip) : key.ip == null;
        }

        @Override
        public int hashCode() {
            int result = meta != null ? meta.hashCode() : 0;
            result = 31 * result + (ip != null ? ip.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "meta=" + meta +
                    ", ip='" + ip + '\'' +
                    '}';
        }
    }

    private static class Version {
        private volatile long version;

        public Version(long version) {
            this.version = version;
        }

        public void update(long newVersion) {
            if (newVersion > version) {
                synchronized (this) {
                    if (newVersion > version) {
                        version = newVersion;
                    }
                }
            }
        }

        public long get() {
            return version;
        }
    }
}
