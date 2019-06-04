package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.longpolling.LongPollingStore;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.domain.UpdateType;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 14:34
 */
@Service
public class CacheConfigVersionServiceImpl implements CacheConfigVersionService {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfigVersionServiceImpl.class);

    @Resource
    private ConfigDao configDao;

    @Resource
    private LongPollingStore longPollingStore;

    private volatile ConcurrentMap<ConfigMeta, Long> cache = Maps.newConcurrentMap();

    @PostConstruct
    public void init() {
        freshConfigVersionCache();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("fresh-config-version-thread");
                try {
                    freshConfigVersionCache();
                } catch (Throwable e) {
                    logger.error("fresh config version error", e);
                }
            }
        }, 3, 3, TimeUnit.MINUTES);
    }

    @Override
    public Optional<Long> getVersion(ConfigMeta meta) {
        return Optional.fromNullable(cache.get(meta));
    }

    @Override
    @SuppressWarnings("all")
    public void update(VersionData<ConfigMeta> configId, UpdateType updateType) {
        ConfigMeta key = configId.getData();
        if (updateType == UpdateType.DELETE) {
            cache.remove(key);
            return;
        }

        Long version = cache.get(key);
        long newVersion = configId.getVersion();
        if (version != null && version >= newVersion) return;

        synchronized (this) {
            version = cache.get(key);
            if (version == null || newVersion > version) {
                cache.put(key, newVersion);
                longPollingStore.onChange(key, newVersion);
            }
        }
    }

    private void freshConfigVersionCache() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            logger.info("fresh config version cache");
            List<VersionData<ConfigMeta>> configIds = configDao.loadAll();

            ConcurrentMap<ConfigMeta, Long> newCache = new ConcurrentHashMap<ConfigMeta, Long>(configIds.size());
            ConcurrentMap<ConfigMeta, Long> oldCache = this.cache;

            synchronized (this) {
                for (VersionData<ConfigMeta> configId : configIds) {
                    long newVersion = configId.getVersion();
                    Long oldVersion = cache.get(configId.getData());
                    // 暂时不考虑delete的情况
                    // 从数据库load数据先于配置更新
                    if (oldVersion != null && oldVersion > newVersion) {
                        newVersion = oldVersion;
                    }
                    newCache.put(configId.getData(), newVersion);
                }

                this.cache = newCache;
            }

            logger.info("fresh config version cache successOf, count [{}]", configIds.size());
            int updates = 0;
            for (Map.Entry<ConfigMeta, Long> oldEntry : oldCache.entrySet()) {
                ConfigMeta meta = oldEntry.getKey();
                Long oldVersion = oldEntry.getValue();
                Long newVersion = newCache.get(meta);
                if (newVersion != null && newVersion > oldVersion) {
                    updates += 1;
                    longPollingStore.onChange(meta, newVersion);
                }
            }
            logger.info("fresh size={} config version cache from db", updates);
        } finally {
            Monitor.freshConfigVersionCacheTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
