package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.metrics.QConfigTimer;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhenyu.nie created on 2014 2014/7/3 18:58
 */
@Service
public class CacheServiceImpl implements CacheService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //由于引用关系被存放在多个容器，所以需要一个同步锁
    private ReadWriteLock referenceLock = new ReentrantReadWriteLock();
    private volatile ConcurrentMap<ConfigMeta, ConfigMeta> currentReferenceCache = Maps.newConcurrentMap();
    private volatile ConcurrentMap<ConfigMeta, ConfigMeta> currentChild2ParentInheritCache = Maps.newConcurrentMap();
    private volatile Map<ConfigMeta, Set<ConfigMeta>> currentParent2ChildrenInheritCache = Maps.newConcurrentMap();

    @Resource
    private ConfigDao configDao;

    @PostConstruct
    public void after() {
        updateReferenceCache();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("update_local_reference_cache_thread");
                try {
                    updateReferenceCache();
                } catch (Throwable e) {
                    logger.error("update reference cache error", e);
                }
            }
        }, 3, 3, TimeUnit.MINUTES);
    }

    private void updateReferenceCache() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            logger.info("update reference cache");
            List<ReferenceInfo> referenceInfos = configDao.loadAllReferenceInfo();
            ConcurrentMap<ConfigMeta, ConfigMeta> newReferenceCache = new ConcurrentHashMap<ConfigMeta, ConfigMeta>(referenceInfos.size());
            ConcurrentMap<ConfigMeta, ConfigMeta> newChild2ParentInheritCache = new ConcurrentHashMap<ConfigMeta, ConfigMeta>(referenceInfos.size());
            Map<ConfigMeta, Set<ConfigMeta>> newParent2ChildrenInheritCache = Maps.newConcurrentMap();

            for (ReferenceInfo referenceInfo : referenceInfos) {
                ConfigMeta source = referenceInfo.getSource();
                ConfigMeta target = referenceInfo.getReference();

                switch (referenceInfo.getRefType()) {
                    case REFERENCE: {
                        newReferenceCache.put(source, target);
                        break;
                    }
                    case INHERIT: {
                        newChild2ParentInheritCache.put(source, target);
                        if(!newParent2ChildrenInheritCache.containsKey(target)) {
                            newParent2ChildrenInheritCache.put(target, Sets.<ConfigMeta>newHashSet());
                        }
                        newParent2ChildrenInheritCache.get(target).add(source);
                    }
                }

            }

            referenceLock.writeLock().lock();
            try {
                this.currentReferenceCache = newReferenceCache;
                this.currentChild2ParentInheritCache = newChild2ParentInheritCache;
                this.currentParent2ChildrenInheritCache = newParent2ChildrenInheritCache;
            } finally {
                referenceLock.writeLock().unlock();
            }
            logger.info("update reference cache successOf, count={}", referenceInfos.size());
        } finally {
            Monitor.updateReferenceCache.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void updateReferenceCache(ReferenceInfo referenceInfo, RefChangeType changeType) {
        referenceLock.writeLock().lock();
        try {
            switch (changeType) {
                case ADD:
                    if (referenceInfo.getRefType() == RefType.REFERENCE) {
                        currentReferenceCache.put(referenceInfo.getSource(), referenceInfo.getReference());
                    } else if (referenceInfo.getRefType() == RefType.INHERIT) {
                        currentChild2ParentInheritCache.put(referenceInfo.getSource(), referenceInfo.getReference());
                        if(!currentParent2ChildrenInheritCache.containsKey(referenceInfo.getReference())) {
                            currentParent2ChildrenInheritCache.put(referenceInfo.getReference(), Sets.<ConfigMeta>newConcurrentHashSet());
                        }
                        currentParent2ChildrenInheritCache.get(referenceInfo.getReference()).add(referenceInfo.getSource());
                    }
                    break;
                case REMOVE:
                    if (referenceInfo.getRefType() == RefType.REFERENCE) {
                        currentReferenceCache.remove(referenceInfo.getSource(), referenceInfo.getReference());
                    } else if (referenceInfo.getRefType() == RefType.INHERIT) {
                        if(currentParent2ChildrenInheritCache.containsKey(referenceInfo.getReference())) {
                            currentParent2ChildrenInheritCache.get(referenceInfo.getReference()).remove(referenceInfo.getSource().getGroup());
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("illegal change type: " + changeType + " for " + referenceInfo);
            }
        } finally {
            referenceLock.writeLock().unlock();
        }
    }

    @Override
    public Optional<ConfigMeta> getReference(ConfigMeta configMeta) {
        return Optional.fromNullable(currentReferenceCache.get(configMeta));
    }


    @Override
    public Optional<ConfigMeta> getParent(ConfigMeta childFile) {
        return Optional.fromNullable(currentChild2ParentInheritCache.get(childFile));
    }

    @Override
    public Set<ConfigMeta> getChildren(ConfigMeta parent) {
        Set<ConfigMeta> children = currentParent2ChildrenInheritCache.get(parent);
        return children != null ? ImmutableSet.copyOf(children) : ImmutableSet.<ConfigMeta>of();
    }
}
