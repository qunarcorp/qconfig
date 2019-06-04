package qunar.tc.qconfig.client.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.DataLoader;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServiceFinder;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-9.
 */
abstract class AbstractDataLoader implements DataLoader {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataLoader.class);

    private static final ConcurrentMap<Meta, Version> VERSIONS = new ConcurrentHashMap<Meta, Version>();

    private static final ConcurrentMap<String, FileStore> USED_CONFIGS = new ConcurrentHashMap<String, FileStore>();

    //qconfig的配置变更listener在这个线程池里执行
    private static final Executor EXECUTOR = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("qconfig-worker#"));

    private static final LongPoller LONGPOLLER = new LongPoller(USED_CONFIGS, VERSIONS, new LongPoller.ConfigChangedCallback() {
        @Override
        public Optional<CountDownLatch> onChanged(Map<Meta, VersionProfile> map, TypedCheckResult changed) {
            return loadIfUpdated(map, changed);
        }
    });

    private static final QConfigServerClient CLIENT = QConfigServerClientFactory.create();
    private static final ConfigLogger CONFIG_LOGGER = new HttpConfigLogger(CLIENT);

    static {
        preLoadLocal();
        LONGPOLLER.start();
    }

    /**
     * 根据本地之前缓存的配置预加载
     */
    private static void preLoadLocal() {
        //读取本地所有[版本文件]
        final Map<Meta, VersionProfile> localVersions = FileStore.findAllFiles();

        for (Map.Entry<Meta, VersionProfile> entry : localVersions.entrySet()) {
            VERSIONS.put(entry.getKey(), new Version(entry.getValue()));
        }

        try {
            Optional<CountDownLatch> holder = checkUpdates(new HashMap<Meta, VersionProfile>(localVersions));
            if (!holder.isPresent()) return;

            CountDownLatch latch = holder.get();
            latch.await(2, TimeUnit.SECONDS);
        } catch (Throwable e) {
            LOG.warn("初始化出错，强制载入本地缓存配置!", e);
            forceLoadLocalCache(localVersions);
        }
    }

    private static void forceLoadLocalCache(final Map<Meta, VersionProfile> map) {
        for (final Meta meta : map.keySet()) {
            final Version ver = VERSIONS.get(meta);
            if (ver != null) {
                ver.setLoaded();
            }
        }
    }

    static void setVersion(Meta meta, VersionProfile version) {
        Version v = VERSIONS.get(meta);
        if (v != null) {
            v.forceUpdated(version);
        }
    }

    private static Optional<CountDownLatch> checkUpdates(Map<Meta, VersionProfile> versions) throws Exception {
        if (versions == null || versions.isEmpty()) return Optional.absent();

        TypedCheckResult remote = CLIENT.checkUpdate(versions).get();

        return loadIfUpdated(versions, remote);
    }

    private static Optional<CountDownLatch> loadIfUpdated(Map<Meta, VersionProfile> versions, TypedCheckResult remote) {
        final CountDownLatch latch = new CountDownLatch(versions.size());

        for (Map.Entry<Meta, VersionProfile> entry : versions.entrySet()) {
            final Meta key = entry.getKey();
            final Version localVersion = VERSIONS.get(key);
            VersionProfile remoteVersion = remote.getResult().get(key);
            loadIfUpdated(key, localVersion, remoteVersion, latch);
        }

        return Optional.of(latch);
    }

    private static void loadIfUpdated(Meta fileMeta, Version localVersion, VersionProfile remoteVersion, CountDownLatch latch) {
        if (localVersion == null) {
            latch.countDown();
            return;
        }

        if (localVersion.updated.get().needUpdate(remoteVersion)) {
            updateFile(fileMeta, remoteVersion, latch);
        } else {
            latch.countDown();
            if (remoteVersion != null && remoteVersion.getVersion() <= Constants.PURGE_FILE_VERSION) {
                FileStore.purgeAllRelativeFiles(fileMeta);
            }
            localVersion.setLoaded();
        }
    }

    private static void updateFile(final Meta meta, final VersionProfile newVersion, final CountDownLatch latch) {
        if (foundInLocal(newVersion, meta)) {
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    updateVersion(meta, newVersion, latch, null);
                    setLoaded(meta);
                }
            });

            return;
        }

        final FileStore fileStore = USED_CONFIGS.get(meta.getKey());
        final ListenableFuture<Snapshot<String>> future = CLIENT.loadData(meta, newVersion, fileStore == null ? Feature.DEFAULT : fileStore.getFeature());
        future.addListener(new Runnable() {
            public void run() {
                try {
                    Snapshot<String> snapshot = future.get();
                    try {
                        FileStore.storeData(meta, newVersion, snapshot);
                    } catch (Throwable e) {
                        LOG.warn("缓存配置到本地磁盘失败", meta);
                        latch.countDown();
                        return;
                    }
                    updateVersion(meta, newVersion, latch, snapshot);
                } catch (Throwable e) {
                    LOG.warn("获取文件错误!", e);
                } finally {
                    setLoaded(meta);
                }
            }
        }, EXECUTOR);
    }

    private static boolean foundInLocal(VersionProfile newVersion, Meta meta) {
        final File file = FileStore.getSnapshotFile(newVersion, meta.getGroupName(), meta.getFileName());
        return file.exists() && file.canRead();
    }

    private static void setLoaded(Meta meta) {
        Version version = VERSIONS.get(meta);
        if (version != null) version.setLoaded();
    }

    private static void updateVersion(final Meta meta, final VersionProfile newVersion, CountDownLatch latch, Snapshot<String> snapshot) {
        Version ver = VERSIONS.get(meta);

        if (ver == null) {
            VERSIONS.putIfAbsent(meta, new Version(VersionProfile.ABSENT));
            ver = VERSIONS.get(meta);
        }

        VersionProfile uVer = ver.updated.get();
        boolean versionChanged = uVer.needUpdate(newVersion) && ver.updated.compareAndSet(uVer, newVersion);
        latch.countDown();
        if (versionChanged) {
            FileStore store = USED_CONFIGS.get(meta.getKey());
            if (store != null)
                versionChanged(store, snapshot);
        }
    }

    private static void versionChanged(FileStore store, Snapshot<String> snapshot) {
        VersionProfile version = VERSIONS.get(store.getMeta()).updated.get();
        try {
            store.setVersion(version, snapshot);
        } catch (Exception e) {
            LOG.warn("文件载入失败: meta: {}, version: {}", store.getMeta(), version, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Configuration<T> load(String groupName, String fileName, Feature feature, Generator<T> generator) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "fileName必须提供");

        fileName = fileName.toLowerCase();
        FileChecker.checkName(fileName);

        if (groupName == null) {
            groupName = getGroupName();
        }

        String key = Meta.createKey(groupName, fileName);
        FileStore store = USED_CONFIGS.get(key);
        if (store != null) return store.getConfig();

        AbstractConfiguration<T> conf = generator.create(feature, fileName);
        FileStore newer = new FileStore(new Meta(groupName, fileName), conf, feature, CONFIG_LOGGER);
        store = USED_CONFIGS.putIfAbsent(key, newer);
        if (store == null) newer.init();

        if (store != null) return store.getConfig();

        final FileStore temp = store = newer;
        if (store.isLoaded()) return store.getConfig();

        Version ver = VERSIONS.get(store.getMeta());
        if (ver != null) {
            final Version nVer = ver;
            nVer.addListener(new Runnable() {
                @Override
                public void run() {
                    ListenableFuture<VersionProfile> future = temp.initLoad(nVer.updated.get(), CLIENT, EXECUTOR);
                    try {
                        nVer.setUpdated(future.get());
                    } catch (Exception e) {
                        LOG.error("init load config failed", e);
                    }
                }
            }, EXECUTOR);
        } else {
            Version old = VERSIONS.putIfAbsent(store.getMeta(), new Version(VersionProfile.ABSENT));
            if (old == null) {
                final ListenableFuture<VersionProfile> future = temp.initLoad(VersionProfile.ABSENT, CLIENT, EXECUTOR);
                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            VersionProfile newVer = future.get();
                            Version ver = VERSIONS.get(temp.getMeta());
                            ver.setUpdated(newVer);
                        } catch (Exception e) {
                            LOG.error("init load config failed", e);
                        }
                    }
                }, Constants.CURRENT_EXECUTOR);

            }
        }
        return store.getConfig();
    }

    @Override
    public <T> Configuration<T> load(String fileName, Feature feature, Generator<T> generator) {
        return load(null, fileName, feature, generator);
    }

    @Override
    public <T> Configuration<T> load(String fileName, Generator<T> generator) {
        return load(null, fileName, null, generator);
    }

    public abstract String getGroupName();

    static class Version extends AbstractFuture<Boolean> {

        final AtomicReference<VersionProfile> updated = new AtomicReference<VersionProfile>(VersionProfile.ABSENT);

        Version(VersionProfile versionProfile) {
            this.updated.set(versionProfile);
        }

        void setUpdated(VersionProfile value) {
            VersionProfile old = updated.get();
            if (old.needUpdate(value)) {
                this.updated.compareAndSet(old, value);
            }
        }

        void forceUpdated(VersionProfile version) {
            this.updated.set(version);
        }

        //启动时异步加载文件，调用该方法表示已经加载成功
        void setLoaded() {
            set(true);
        }
    }


    static void warmUpFiles(TypedCheckResult result) {
        Map<Meta, VersionProfile> remoteFiles = result.getResult();
        final CountDownLatch latch = new CountDownLatch(remoteFiles.size());

        for (Map.Entry<Meta, VersionProfile> entry : remoteFiles.entrySet()) {
            final Meta fileMeta = entry.getKey();
            final Version localVersion = VERSIONS.get(fileMeta);
            VersionProfile remoteVersion = remoteFiles.get(fileMeta);

            loadIfUpdated(fileMeta, localVersion, remoteVersion, latch);
        }
    }
}
