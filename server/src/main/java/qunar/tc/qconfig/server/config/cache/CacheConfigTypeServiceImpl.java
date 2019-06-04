package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.server.dao.FilePublicStatusDao;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 14:00
 */
@Service
public class CacheConfigTypeServiceImpl implements CacheConfigTypeService {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfigTypeServiceImpl.class);

    @Resource
    private FilePublicStatusDao filePublicStatusDao;

    private volatile Map<ConfigMetaWithoutProfile, PublicType> cache = Maps.newConcurrentMap();

    @PostConstruct
    public void init() {
        freshConfigTypeCache();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("fresh-config-type-thread");
                try {
                    freshConfigTypeCache();
                } catch (Throwable e) {
                    logger.error("fresh config type cache error", e);
                }
            }
        }, 3, 3, TimeUnit.MINUTES);
    }

    @Override
    public Optional<PublicType> getType(String group, String dataId) {
        return Optional.fromNullable(cache.get(new ConfigMetaWithoutProfile(group, dataId)));
    }

    @Override
    public void update(PublicConfigInfo configInfo) {
        boolean exist = filePublicStatusDao.exist(configInfo);
        if (exist) {
            cache.put(configInfo.getConfigMetaWithoutProfile(), configInfo.getPublicType());
        } else {
            cache.remove(configInfo.getConfigMetaWithoutProfile());
        }
    }

    private void freshConfigTypeCache() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            logger.info("start fresh config type cache");
            List<PublicConfigInfo> configs = filePublicStatusDao.loadAll();
            Map<ConfigMetaWithoutProfile, PublicType> newCache = Maps.newConcurrentMap();
            for (PublicConfigInfo config : configs) {
                newCache.put(config.getConfigMetaWithoutProfile(), config.getPublicType());
            }
            logger.info("fresh config type cache successOf, count [{}]", configs.size());
            this.cache = newCache;
        } finally {
            Monitor.updateConfigTypeCache.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
