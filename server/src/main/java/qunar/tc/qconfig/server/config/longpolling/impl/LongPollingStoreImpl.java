package qunar.tc.qconfig.server.config.longpolling.impl;

import com.google.common.base.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.LongPollingStore;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 18:04
 */
@Service
public class LongPollingStoreImpl implements LongPollingStore {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingStoreImpl.class);

    private static final ConcurrentMap<ConfigMeta, Cache<Listener, Listener>> listenerMappings = Maps.newConcurrentMap();

    private static final int DEFAULT_THREAD_COUNT = 4;

    private static final long DEFAULT_TIMEOUT = 60 * 1000L;

    private volatile long timeout = DEFAULT_TIMEOUT;

    private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(
            DEFAULT_THREAD_COUNT, new NamedThreadFactory("qconfig-config-listener-push"));

    private static ExecutorService onChangeExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("config-on-change"));

    private static volatile PushConfig pushConfig;

    @Resource(name = "cacheConfigInfoService")
    private ConfigInfoService cacheConfigInfoService;

    @Resource
    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    private static Set<String> closeFixVersionAppidSet = Sets.newHashSet();

    static {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                String maxStr = conf.get("push.server.max");
                int pushMax = Numbers.toInt(maxStr, 0);
                String intervalStr = conf.get("push.server.interval");
                long pushInterval = Numbers.toLong(intervalStr, 0);
                String directPushLimitStr = conf.get("push.server.directPushLimit");
                int directPushLimit = Numbers.toInt(directPushLimitStr, 0);
                if (pushMax <= 0 || pushInterval <= 0) {
                    logger.error("get server push config error, push max [{}], interval [{}], direct push limit [{}]", maxStr, intervalStr, directPushLimitStr);
                } else {
                    pushConfig = new PushConfig(pushMax, pushInterval, directPushLimit);
                    logger.info("get server push config, push max [{}], interval [{}], direct push limit [{}]", pushMax, pushInterval, directPushLimit);
                }

                String closeFixVersionAppidList = conf.get("close.fix.version.appids");
                if (!Strings.isNullOrEmpty(closeFixVersionAppidList)) {
                    closeFixVersionAppidSet = Sets.newHashSet(Splitter.on(",").splitToList(closeFixVersionAppidList));
                }
            }
        });
        Preconditions.checkNotNull(pushConfig, "server push config should not be null");

        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    String oldName = Thread.currentThread().getName();
                    Thread.currentThread().setName("qconfig-config-listener-clearUp");
                    try {
                        for (Cache<Listener, Listener> cache : listenerMappings.values()) {
                            cache.cleanUp();
                        }
                    } finally {
                        Thread.currentThread().setName(oldName);
                    }
                } catch (Exception e) {
                    logger.error("schedule listener clear up error", e);
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    @PostConstruct
    public void init() {
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
    public void addListener(Listener listener) {
        Cache<Listener, Listener> cache = getOrCreateCache(listener.getMeta());
        cache.put(listener, listener);
    }

    private Cache<Listener, Listener> getOrCreateCache(ConfigMeta meta) {
        Cache<Listener, Listener> listeners = listenerMappings.get(meta);
        if (listeners == null) {
            Cache<Listener, Listener> newListeners = CacheBuilder
                    .newBuilder()
                    .expireAfterWrite(timeout + 100, TimeUnit.MILLISECONDS)
                    .build();
            listeners = listenerMappings.putIfAbsent(meta, newListeners);
            if (listeners == null) {
                listeners = newListeners;
            }
        }
        return listeners;
    }

    @Override
    public void manualPush(ConfigMeta meta, long version, final Set<IpAndPort> ipAndPorts) {
        logger.info("push client file: {}, version {}, {}", meta, version, ipAndPorts);
        Set<String> ips = Sets.newHashSetWithExpectedSize(ipAndPorts.size());
        for (IpAndPort ipAndPort : ipAndPorts) {
            ips.add(ipAndPort.getIp());
        }

        manualPushIps(meta, version, ips);
    }

    @Override
    public void manualPushIps(ConfigMeta meta, long version, final Set<String> ips) {
        logger.info("push client file: {}, version {}, {}", meta, version, ips);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            doChange(meta, version, Constants.PULL, new Predicate<Listener>() {
                @Override
                public boolean apply(Listener input) {
                    return ips.contains(input.getContextHolder().getIp());
                }
            });
        } finally {
            Monitor.filePushOnChangeTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onChange(final ConfigMeta meta, final long version) {
        logger.info("file change: {}, version {}", meta, version);
        onChangeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Stopwatch stopwatch = Stopwatch.createStarted();
                try {
                    doChange(meta, version, Constants.UPDATE, Predicates.<Listener>alwaysTrue());
                } finally {
                    Monitor.fileOnChangeTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    private void doChange(ConfigMeta meta, long newVersion, String type, Predicate<Listener> needChange) {
        List<Listener> listeners = getListeners(meta, needChange);
        if (listeners.isEmpty()) {
            return;
        }

        Changed change = new Changed(meta, newVersion);
        if (listeners.size() <= pushConfig.getDirectPushLimit()) {
            directDoChange(listeners, change, type);
        } else {
            PushItem pushItem = new PushItem(listeners, type, change);
            scheduledExecutor.execute(new PushRunnable(pushItem));
        }
    }

    private List<Listener> getListeners(ConfigMeta meta, Predicate<Listener> needChange) {
        List<Listener> selfListeners = getListenersForMeta(meta, needChange);
        List<Listener> childrenListeners = getChildrenListeners(meta, needChange);
        List<Listener> beforeFilter = ImmutableList.copyOf(Iterables.concat(selfListeners, childrenListeners));
        if (closeFixVersionAppidSet.contains(meta.getGroup())) {
            return beforeFilter;
        } else {
            return fixListeners(beforeFilter, meta);
        }
    }

    private List<Listener> fixListeners(final List<Listener> concatListeners, final ConfigMeta meta) {
        return FluentIterable.from(concatListeners).filter(
                new Predicate<Listener>() {
                    @Override
                    public boolean apply(Listener listener) {
                        String ip = listener.getContextHolder().getIp();
                        return !cacheFixedVersionConsumerService.getFixedVersion(meta, ip).isPresent();
                    }
                }
        ).toList();
    }

    private List<Listener> getChildrenListeners(ConfigMeta meta, Predicate<Listener> needChange) {
        List<Listener> listeners = Lists.newArrayList();
        Set<ConfigMeta> children = cacheConfigInfoService.getChildren(meta);
        for (ConfigMeta child : children) {
            listeners.addAll(getListenersForMeta(child, needChange));
        }
        return listeners;
    }

    private List<Listener> getListenersForMeta(ConfigMeta meta, Predicate<Listener> needChange) {
        Cache<Listener, Listener> cache = listenerMappings.get(meta);
        if (cache != null) {
            List<Listener> listeners = Lists.newArrayList();
            for (Listener listener : cache.asMap().values()) {
                if (needChange.apply(listener)) {
                    cache.invalidate(listener);
                    listeners.add(listener);
                }
            }
            cache.cleanUp();
            return listeners;
        } else {
            return ImmutableList.of();
        }
    }

    private void directDoChange(List<Listener> listeners, Changed change, String type) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            for (Listener listener : listeners) {
                logger.debug("return {}, {}", listener, change);
                returnChange(change, listener, type);
            }
        } catch (Exception e) {
            Monitor.batchReturnChangeFailCounter.inc();
            logger.error("batch direct return changes error, type {}, change {}", type, change, e);
        } finally {
            Monitor.batchReturnChangeTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private static class PushRunnable implements Runnable {

        private final PushItem pushItem;

        private PushRunnable(PushItem pushItem) {
            this.pushItem = pushItem;
        }

        @Override
        public void run() {
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                long start = System.currentTimeMillis();
                PushConfig config = pushConfig;
                int num = Math.min(pushItem.getListeners().size(), config.getPushMax());
                for (int i = 0; i < num; ++i) {
                    Listener listener = pushItem.getListeners().poll();
                    returnChange(pushItem.getChange(), listener, pushItem.getType());
                }

                if (!pushItem.getListeners().isEmpty()) {
                    long elapsed = System.currentTimeMillis() - start;
                    long delay;
                    if (elapsed >= config.getPushInterval()) {
                        delay = 0;
                    } else {
                        delay = config.getPushInterval() - elapsed;
                    }
                    scheduledExecutor.schedule(new PushRunnable(pushItem), delay, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                Monitor.batchReturnChangeFailCounter.inc();
                logger.error("batch return changes error, {}", pushItem, e);
            } finally {
                Monitor.batchReturnChangeTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private static void returnChange(Changed change, Listener listener, String type) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            listener.onChange(change, type);
        } finally {
            Monitor.returnChangeTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

}
