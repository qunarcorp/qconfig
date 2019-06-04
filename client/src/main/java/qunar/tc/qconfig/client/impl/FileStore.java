package qunar.tc.qconfig.client.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.FeatureRemote;
import qunar.tc.qconfig.client.exception.ResultUnexpectedException;
import qunar.tc.qconfig.client.util.AppStoreUtil;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.EnvironmentAware;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static qunar.tc.qconfig.client.impl.FileSystem.atomicWriteFile;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-13.
 */
class FileStore<T> {
    private static final Logger log = LoggerFactory.getLogger(FileStore.class);

    private static final File CONF_HOME = new File(AppStoreUtil.getAppStore(), "qconfig/" + EnvironmentAware.determinedEnv());

    private static final String LOCAL_TEST_DIR = "qconfig_test/";

    private static final String SNAPSHOT_DIR = "/snapshot2/";

    private static final String VERSION_EXT = ".ver2";

    private static final int MAX_RETAIN_VERSION = 3;

    private final Meta meta;

    private final AbstractConfiguration<T> conf;

    private final Feature feature;

    private final ConfigLogger configLogger;

    private volatile long lastCheck = -1;

    private static final String COMMA = ",";

    private static final Splitter COMMA_SPLITTER = Splitter.on(COMMA).omitEmptyStrings().trimResults();

    private static final TemplateTool templateTool;

    private static final ConcurrentMap<Meta, Object> purgedFiles = new ConcurrentHashMap<Meta, Object>();

    static final FileVersion ABSENT = new FileVersion(FileVersion.Type.remote, VersionProfile.ABSENT);

    private static ConcurrentMap<Meta, LatestSnapshot> memory = new ConcurrentHashMap<Meta, LatestSnapshot>();

    static {
        try {
            if (!CONF_HOME.exists()) {
                CONF_HOME.mkdirs();
            }
            CONF_HOME.setReadable(false, false);
            CONF_HOME.setReadable(true, true);
        } catch (Exception e) {
            log.warn("初始化配置中心本地目录失败，请检查目录权限 {}", CONF_HOME.getAbsolutePath(), e);
        }

        templateTool = new TemplateTool();
    }

    private AtomicReference<FileVersion> currentVersion = new AtomicReference<FileVersion>();

    private static ConfigRepository configRepository = ConfigRepository.getInstance();

    FileStore(Meta meta, AbstractConfiguration conf, Feature feature, ConfigLogger configLogger) {

        this.meta = meta;
        this.feature = feature == null ? Feature.DEFAULT : feature;
        this.conf = conf;
        this.configLogger = configLogger;
    }

    void init() {
        checkOverride(0);
    }

    boolean isLoaded() {
        return currentVersion.get() != null;
    }

    FileVersion currentVersion() {
        return currentVersion.get();
    }

    AbstractConfiguration<?> getConfig() {
        return conf;
    }

    synchronized ListenableFuture<VersionProfile> initLoad(final VersionProfile version, QConfigServerClient client, Executor executor) {
        final SettableFuture<VersionProfile> result = SettableFuture.create();

        File file = getSnapshotFile(version);
        if (version != VersionProfile.ABSENT && version.getVersion() >= feature.getMinimumVersion() && file.exists() && file.canRead()) {
            try {
                setVersion(version, null);
                result.set(version);
                return result;
            } catch (Exception e) {
                log.warn("初始化尝试载入解析最高版本失败 {},{}", meta, version, e);
            }
        }

        final ListenableFuture<Snapshot<String>> future = client.forceReload(meta, feature.getMinimumVersion(), feature);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                RuntimeException re = null;
                try {
                    Snapshot<String> snapshot = future.get();

                    VersionProfile remoteVersion = snapshot.getVersion();
                    if (remoteVersion.getVersion() < feature.getMinimumVersion())
                        throw new Exception("forceLoad 返回版本小于要求的最低版本 " + remoteVersion + "/" + feature.getMinimumVersion());

                    storeData(meta, remoteVersion, snapshot);
                    setVersion(remoteVersion, snapshot);
                    result.set(remoteVersion);
                    return;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause != null && cause instanceof ResultUnexpectedException) {
                        ResultUnexpectedException ex = (ResultUnexpectedException) cause;
                        if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                            if (feature.isFailOnNotExists()) {
                                log.warn("你所需要的配置在qconfig上不存在: {} minVersion:{} ", meta, feature.getMinimumVersion());
                                conf.setException(ex);
                            } else {
                                conf.setData(conf.emptyData(), false);
                            }
                            result.set(VersionProfile.ABSENT);
                            return;
                        }
                        re = ex;
                    }
                    log.warn("尝试远程载入最高版本失败 {},{}，等待重试", meta, version, cause);
                } catch (Throwable e) {
                    log.warn("尝试远程载入最高版本失败 {},{}，等待重试", meta, version, e);
                }

                if (loadLastVersion(version, result)) return;

                if (feature.isFailOnNotExists()) {
                    conf.setException(re == null ? new RuntimeException("加载配置文件失败") : re);
                } else {
                    conf.setData(conf.emptyData(), false);
                }

                result.set(VersionProfile.ABSENT);
            }
        }, executor);

        return result;
    }

    private boolean loadLastVersion(VersionProfile currentVersion, SettableFuture<VersionProfile> result) {
        VersionProfile last = readVersion(getVersionFile());
        File file = getSnapshotFile(last);
        if (!last.equals(currentVersion) && last.getVersion() >= feature.getMinimumVersion() && file.exists() && file.canRead()) {
            try {
                setVersion(last, null);
                result.set(last);
                return true;
            } catch (Exception e) {
                log.warn("降级到本地缓存版本失败 {},{}", meta, last, e);
            }
        }

        return false;
    }

    public Feature getFeature() {
        return feature;
    }

    synchronized void setVersion(VersionProfile version, Snapshot<String> snapshot) throws Exception {
        String data;

        if (snapshot != null) {
            data = snapshot.getContent();
        } else {
            data = loadSnapshot(version);
        }

        data = templateTool.merge(meta.getFileName(), data);

        T t;
        try {
            t = conf.parse(data);
        } catch (Throwable e) {
            configLogger.log(ConfigLogType.PARSE_REMOTE_ERROR, meta, version.getVersion(), e);
            throw new RuntimeException(e);
        }

        FileVersion current = currentVersion.get();
        FileVersion newVer = new FileVersion(FileVersion.Type.remote, version);

        if (FileVersion.needUpdate(current, newVer) && currentVersion.compareAndSet(current, newVer)) {

            if (storeAtLocal(snapshot)) {
                saveToConfigRepository(meta, newVer.getVersion().getVersion(), data);
                File versionFile = getVersionFile();
                atomicWriteFile(versionFile, Long.toString(version.getVersion(), 10) + COMMA + version.getProfile());
            }

            //触发配置变更逻辑
            boolean success = conf.setData(t);

            log.info("use remote file, name={}, version={}", meta.getFileName(), version);
            String message = Constants.EMPTY;
            if (!success) {
                message = "listener error";
            }
            configLogger.log(ConfigLogType.USE_REMOTE_FILE, meta, version.getVersion(), message);
            purge(version);
            purgedFiles.remove(meta);
        }
    }

    static void purgeAllRelativeFiles(Meta meta) {
        if (purgedFiles.putIfAbsent(meta, Boolean.TRUE) != null) return;

        log.info("start clear relative unused cache file: [{}]", meta);
        try {
            File snapshotDir = new File(CONF_HOME, meta.getGroupName() + SNAPSHOT_DIR);
            File[] sameNameFiles = snapshotDir.listFiles(sameNameFilter(meta.getFileName()));
            if (sameNameFiles != null) {
                for (File file : sameNameFiles) {
                    deleteWithLog(file);
                }
            }

            File versionFile = getVersionFile(meta);
            if (versionFile.exists()) {
                deleteWithLog(versionFile);
            }
        } catch (Exception e) {
            log.error("clear relative unused cache files error, [{}]", meta, e);
        }
    }

    private static void deleteWithLog(File file) {
        boolean delete = file.delete();
        if (delete) {
            log.info("clear unused cache file successOf: {}", file.getAbsolutePath());
        } else {
            log.warn("clear unused cache file failOf: {}", file.getAbsolutePath());
        }
    }

    private void purge(VersionProfile currentVersion) {
        final long maxPurgeVersion = currentVersion.getVersion() - MAX_RETAIN_VERSION;
        if (maxPurgeVersion <= 0) return;

        File snapshotDir = new File(CONF_HOME, meta.getGroupName() + SNAPSHOT_DIR);
        try {
            File[] sameNameFiles = snapshotDir.listFiles(sameNameAndProfileFilter(currentVersion.getProfile()));
            if (sameNameFiles == null || sameNameFiles.length <= MAX_RETAIN_VERSION) return;
            Arrays.sort(sameNameFiles, FILE_ORDERING);
            for (int i = MAX_RETAIN_VERSION; i < sameNameFiles.length; ++i) {
                log.info("start clean stale config file, {}", sameNameFiles[i].getName());
                sameNameFiles[i].delete();
            }
        } catch (Exception e) {
            log.error("清理历史版本配置文件出现错误", e);
        }

    }

    private static final Ordering<File> FILE_ORDERING = Ordering.natural().reverse().onResultOf(new Function<File, Comparable>() {
        @Override
        public Comparable apply(File input) {
            String name = input.getName();
            int profilePosition = name.lastIndexOf('.');
            name = name.substring(0, profilePosition);
            int versionPosition = name.lastIndexOf('.');
            return Integer.parseInt(name.substring(versionPosition + 1));
        }
    });


    boolean checkOverride(long interval) {
        File override = getOverrideFile();
        boolean hasOverride = override != null;

        long now = System.currentTimeMillis();
        if (now - lastCheck < interval) {
            return hasOverride;
        }

        lastCheck = now;

        FileVersion current = currentVersion.get();

        if (hasOverride) {
            FileVersion oVersion = new FileVersion(FileVersion.Type.override, new VersionProfile(override.lastModified(), VersionProfile.LOCAL_PROFILE));
            synchronized (this) {
                while (FileVersion.needUpdate(current, oVersion)) {
                    if (currentVersion.compareAndSet(current, oVersion)) {
                        try {
                            log.warn("Override 载入本地文件: {}", override);
                            String content = Files.asCharSource(override, Charsets.UTF_8).read();
                            T data = conf.parse(content);
                            conf.setData(data);
                            ConfigRepository.getInstance().saveOrUpdate(meta, oVersion.getVersion().getVersion(), content);
                            log.info("use override file, name={}, last modified={}", override.getName(), override.lastModified());
                            configLogger.log(ConfigLogType.USE_OVERRIDE, meta, 0);
                        } catch (Exception e) {
                            conf.setException(e);
                            log.error("Override 文件载入失败:{}", override, e);
                        }
                    }
                    current = currentVersion.get();
                }
            }
        } else if (current != null && current.getType() == FileVersion.Type.override) {
            currentVersion.compareAndSet(current, ABSENT); // 在删除本地文件等待恢复远程文件时
            AbstractDataLoader.setVersion(meta, VersionProfile.ABSENT);
        }
        return hasOverride;
    }

    Meta getMeta() {
        return meta;
    }

    private String loadSnapshot(VersionProfile version) throws IOException {
        LatestSnapshot snapshot = memory.get(this.meta);

        if ((snapshot != null)) {
            String content = snapshot.getSnapshot(version);
            if (!Strings.isNullOrEmpty(content)) return content;
        }

        File file = getSnapshotFile(version);
        return Files.asCharSource(file, Charsets.UTF_8).read();
    }

    private File getOverrideFile() {
        return getOverrideFile(meta);
    }

    static File getSnapshotFile(VersionProfile version, String groupName, String fileName) {
        return new File(CONF_HOME, groupName + SNAPSHOT_DIR + fileName + "." + version.getVersion() + "." + profileToFileName(version.getProfile()));
    }

    // 文件名不能有':'

    private static String profileToFileName(String profile) {
        return profile.replace(":", "-");
    }

    static String fileNameToProfile(String fileName) {
        return fileName.replace("-", ":");
    }

    private File getSnapshotFile(VersionProfile version) {
        return getSnapshotFile(version, meta.getGroupName(), meta.getFileName());
    }

    static File getVersionFile(Meta meta) {
        return new File(CONF_HOME, meta.getKey() + VERSION_EXT);
    }

    private File getVersionFile() {
        return getVersionFile(meta);
    }

    private static File getOverrideFile(Meta meta) {
        File file = getDebugFile(meta);
        if (file != null)
            return file;
        return getLocalTestFile(meta);
    }

    private static File getDebugFile(Meta meta) {
        File file = new File(CONF_HOME, meta.getKey() + ".debugging");
        return file.canRead() ? file : null;
    }

    private static File getLocalTestFile(Meta meta) {
        String name = LOCAL_TEST_DIR + meta.getKey();

        try {
            URL path = Thread.currentThread().getContextClassLoader().getResource(name);
            if (path == null)
                return null;
            File file = new File(path.getPath());
            if (!file.exists()) {
                return null;
            }
            if (!file.canRead()) {
                log.warn("配置文件已找到,但是无法读取. {}", file);
            }
            return file;

        } catch (Exception e) {
            log.debug("查找配置文件过程出错 {}", name, e);
        }
        return null;
    }

    static VersionProfile readVersion(File file) {
        try {
            if (file.canRead()) {
                String line = Files.asCharSource(file, Charsets.UTF_8).readFirstLine();
                Iterable<String> strs = COMMA_SPLITTER.split(line);
                Iterator<String> iterator = strs.iterator();
                long version = Long.parseLong(iterator.next());
                String profile = iterator.next();
                return new VersionProfile(version, profile);
            }
        } catch (Exception e) {
            log.warn("无法读取版本文件 {}", file);
        }
        return VersionProfile.ABSENT;
    }

    static Map<Meta, VersionProfile> findAllFiles() {
        Map<Meta, VersionProfile> map = new HashMap<Meta, VersionProfile>();
        try {
            File[] files = CONF_HOME.listFiles();
            if (files == null) {
                log.error("遍历qconfig本地缓存文件目录出错");
                return map;
            }

            for (File groupFile : files) {
                if (!groupFile.isDirectory())
                    continue;

                String groupName = groupFile.getName();

                File[] groupFiles = groupFile.listFiles();
                if (groupFiles == null) {
                    continue;
                }

                for (File file : groupFiles) {
                    String fileName = file.getName();
                    if (!fileName.endsWith(VERSION_EXT)) continue;

                    VersionProfile version = readVersion(file);
                    fileName = fileName.substring(0, fileName.length() - VERSION_EXT.length());

                    Meta meta = new Meta(groupName, fileName);
                    if (getOverrideFile(meta) == null) {
                        map.put(meta, version);
                    }
                }
            }
        } catch (Throwable t) {
            log.warn("初始化错误", t);
        }
        return map;
    }

    private static boolean storeAtLocal(Snapshot<String> snapshot) {
        if (snapshot == null) return true;
        FeatureRemote remoteFeature = snapshot.getFeatureRemote();
        return remoteFeature != null && remoteFeature.isLocalCache();
    }

    static void storeData(Meta meta, VersionProfile version, Snapshot<String> latestSnapshot) throws IOException {
        if (!storeMemory(meta, latestSnapshot)) return;

        if (storeAtLocal(latestSnapshot)) {
            final File file = FileStore.getSnapshotFile(version, meta.getGroupName(), meta.getFileName());
            atomicWriteFile(file, latestSnapshot.getContent());
        }

    }

    private static boolean storeMemory(Meta meta, Snapshot<String> latestSnapshot) {
        LatestSnapshot snapshot = memory.get(meta);
        if (snapshot != null) {
            return snapshot.update(latestSnapshot.getVersion(), latestSnapshot.getContent());
        }

        LatestSnapshot value = new LatestSnapshot();
        LatestSnapshot old = memory.putIfAbsent(meta, value);
        if (old != null) {
            return old.update(latestSnapshot.getVersion(), latestSnapshot.getContent());
        } else {
            return value.update(latestSnapshot.getVersion(), latestSnapshot.getContent());
        }
    }

    private static void saveToConfigRepository(Meta meta, long version, String data) {
        try {
            configRepository.saveOrUpdate(meta, version, data);
        } catch (Exception e) {
            log.warn("Save updated file to config repository failed. Group name = {}, File name = {}", meta.getGroupName(), meta.getFileName());
        }
    }

    private static FilenameFilter sameNameFilter(final String configName) {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Optional<SnapshotFile> file = SnapshotFile.parseFileName(name);
                return file.isPresent() && file.get().getName().equals(configName);
            }
        };
    }

    private FilenameFilter sameNameAndProfileFilter(final String profile) {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Optional<SnapshotFile> file = SnapshotFile.parseFileName(name);
                return file.isPresent() && file.get().getProfile().equals(profile) && file.get().getName().equals(meta.getFileName());
            }
        };
    }

    private static class SnapshotFile {
        private String name;
        private String version;
        private String profile;

        SnapshotFile(String name, String version, String profile) {
            this.name = name;
            this.version = version;
            this.profile = profile;
        }

        static Optional<SnapshotFile> parseFileName(String fileName) {
            if (Strings.isNullOrEmpty(fileName)) {
                return Optional.absent();
            }

            int profilePosition = fileName.lastIndexOf('.');
            if (profilePosition < 0) {
                return Optional.absent();
            }
            String profile = fileNameToProfile(fileName.substring(profilePosition + 1));

            fileName = fileName.substring(0, profilePosition);
            int versionPosition = fileName.lastIndexOf('.');
            if (versionPosition < 0) {
                return Optional.absent();
            }
            String version = fileName.substring(versionPosition + 1);

            String name = fileName.substring(0, versionPosition).toLowerCase();
            if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(version) || Strings.isNullOrEmpty(profile)) {
                return Optional.absent();
            }

            return Optional.of(new SnapshotFile(name, version, profile));
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getProfile() {
            return profile;
        }

        @Override
        public String toString() {
            return "SnapshotFile{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", profile='" + profile + '\'' +
                    '}';
        }
    }

    private static class LatestSnapshot {
        private VersionProfile versionProfile = VersionProfile.ABSENT;

        private String content;

        public synchronized boolean update(VersionProfile versionProfile, String content) {
            if (this.versionProfile.needUpdate(versionProfile)) {
                this.versionProfile = versionProfile;
                this.content = content;
                return true;
            }
            return false;
        }

        String getSnapshot(VersionProfile versionProfile) {
            if (versionProfile.getProfile().equals(this.versionProfile.getProfile())
                    && versionProfile.getVersion() == this.versionProfile.getVersion()) {
                return content;
            }

            return null;
        }
    }
}
