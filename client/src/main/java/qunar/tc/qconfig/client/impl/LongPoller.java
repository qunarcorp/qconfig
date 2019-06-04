package qunar.tc.qconfig.client.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.support.TomcatStateViewer;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaohui.yu
 * 11/16/17
 */
class LongPoller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LongPoller.class);

    private static final TomcatStateViewer TOMCAT_STATE = TomcatStateViewer.getInstance();

    private static final long OVERRIDE_CHECK_INTERVAL = 60 * 1000L;

    private volatile AtomicBoolean initialed = new AtomicBoolean(false);

    private static final QConfigServerClient CLIENT = QConfigServerClientFactory.create();
    private static final Random LONG_POLLING_RANDOM = new Random();
    private static final ScheduledExecutorService LONG_POLLING_EXECUTOR = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("qconfig-poller#"));

    private final Map<String, FileStore> usedConfigs;
    private final Map<Meta, AbstractDataLoader.Version> localVersions;
    private ConfigChangedCallback callback;

    LongPoller(Map<String, FileStore> usedConfigs, Map<Meta, AbstractDataLoader.Version> localVersions, ConfigChangedCallback callback) {
        this.usedConfigs = usedConfigs;
        this.localVersions = localVersions;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (TOMCAT_STATE.isStopped()) {
            try {
                logger.debug("tomcat is stopped, qconfig sleep");
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                logger.warn("tomcat stop sleep interrupted", e);
                return;
            }
        }

        logger.debug("start qconfig reloading");
        try {
            Optional<CountDownLatch> latch = reLoading();
            if (latch.isPresent()) {
                if (!latch.get().await(20, TimeUnit.SECONDS)) {
                    logger.warn("20 seconds elapsed and qconfig file change loading not finish, perhaps something wrong");
                }
                LONG_POLLING_EXECUTOR.execute(this);
            } else {
                long emptyCheckDelay;
                if (initialed.compareAndSet(false, true)) {
                    emptyCheckDelay = 3 * 1000L;
                } else {
                    emptyCheckDelay = 30 * 1000L;
                }
                LONG_POLLING_EXECUTOR.schedule(this, emptyCheckDelay, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            logger.info("long-polling check update error", e);
            long delay = LONG_POLLING_RANDOM.nextInt(60 * 1000);
            LONG_POLLING_EXECUTOR.schedule(this, delay, TimeUnit.MILLISECONDS);
        }
    }

    void start() {
        LONG_POLLING_EXECUTOR.execute(this);
    }

    private Optional<CountDownLatch> reLoading() throws Exception {
        Map<Meta, VersionProfile> map = Maps.newHashMap();

        for (FileStore store : usedConfigs.values()) {
            if (!store.getFeature().isAutoReload()) {
                continue;
            }

            boolean hasOverride = store.checkOverride(OVERRIDE_CHECK_INTERVAL);
            if (hasOverride) {
                continue;
            }

            AbstractDataLoader.Version ver = localVersions.get(store.getMeta());
            map.put(store.getMeta(), ver == null ? VersionProfile.ABSENT : ver.updated.get());
        }

        if (map.isEmpty()) return Optional.absent();

        TypedCheckResult remote = CLIENT.longPollingCheckUpdate(map).get();
        return this.callback.onChanged(map, remote);
    }

    interface ConfigChangedCallback {
        Optional<CountDownLatch> onChanged(Map<Meta, VersionProfile> map, TypedCheckResult changed);
    }
}
