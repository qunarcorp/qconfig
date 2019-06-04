package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.dao.FixedConsumerVersionDao;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.MetaIp;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CacheFixedVersionConsumerServiceImpl implements CacheFixedVersionConsumerService {

    private final Logger logger = LoggerFactory.getLogger(CacheFixedVersionConsumerServiceImpl.class);

    private final static long VERSION_NOT_FIXED = -1L;

    private volatile ConcurrentMap<MetaIp, Long> cache = Maps.newConcurrentMap();

    @Resource
    private FixedConsumerVersionDao fixedConsumerVersionDao;


    @PostConstruct
    private void init() {
        refreshCache();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("fresh-fixed-version-thread");
                refreshCache();
            }
        }, 3, 3, TimeUnit.MINUTES);
    }

    private void refreshCache() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        logger.info("start refreshing fixed version consumer cache");
        try {
            ConcurrentMap<MetaIp, Long> newCache = Maps.newConcurrentMap();
            synchronized (this) {
                newCache.putAll(fixedConsumerVersionDao.queryAll());
                cache = newCache;
            }
            logger.info("refreshing fixed version consumer cache successOf, total num:[{}]", newCache.size());
        } catch (Exception e) {
            logger.error("refreshing fixed version consumer cache error", e);
        } finally {
            Monitor.freshFixedVersionConsumerCache.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public synchronized void update(MetaIp consumer, long version) {
        if (version <= VERSION_NOT_FIXED) {
            cache.remove(consumer);
            logger.info("delete fixed version consumer cache, metaAndIp:{}", consumer);
        } else {
            cache.put(consumer, version);
            logger.info("update fixed version consumer cache, metaAndIp:{}, fixedVersion:{}", consumer, version);
        }
    }

    @Override
    public Optional<Long> getFixedVersion(ConfigMeta meta, String ip) {
        return Optional.fromNullable(cache.get(new MetaIp(meta, ip)));
    }
}
