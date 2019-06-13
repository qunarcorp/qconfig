package qunar.tc.qconfig.server.config.impl;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.metrics.Metrics;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.cache.CacheConfigVersionService;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.config.cache.CachePushConfigVersionService;
import qunar.tc.qconfig.server.config.cache.CacheService;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.dao.FileConfigDao;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.server.domain.UpdateType;
import qunar.tc.qconfig.server.exception.ChecksumFailedException;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.exception.FileDaoProcessException;
import qunar.tc.qconfig.server.support.AddressUtil;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-7
 * Time: 下午2:56
 */
@Service
public class ConfigStoreImpl implements ConfigStore {

    private static final Logger log = LoggerFactory.getLogger(ConfigStoreImpl.class);

    @Resource
    private ConfigDao configDao;

    @Resource
    private FileConfigDao fileConfigDao;

    @Resource
    private CacheService cacheService;

    @Resource
    private CacheConfigVersionService cacheConfigVersionService;

    @Resource
    private CacheConfigInfoService cacheConfigInfoService;

    @Resource
    private DbConfigInfoService dbConfigInfoService;

    @Resource
    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    @Resource
    private CachePushConfigVersionService cachePushConfigVersionService;

    @Resource
    private ClientInfoService clientInfoService;

    private LoadingCache<VersionData<ConfigMeta>, ChecksumData<String>> configCache;

    @PostConstruct
    private void init() {
        configCache = CacheBuilder.newBuilder()
                .maximumSize(5000)
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .recordStats()
                .build(new CacheLoader<VersionData<ConfigMeta>, ChecksumData<String>>() {
                    @Override
                    public ChecksumData<String> load(VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
                        return loadConfig(configId);
                    }
                });

        Metrics.gauge("configFile_notFound_cache_hitRate", new Supplier<Double>() {
            @Override
            public Double get() {
                return configCache.stats().hitRate();
            }
        });
    }

    @Override
    public ChecksumData<String> findConfig(VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        try {
            return configCache.get(configId);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ConfigNotFoundException) {
                throw (ConfigNotFoundException) e.getCause();
            } else {
                log.error("find config error, configId:{}", configId, e);
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private ChecksumData<String> loadConfig(VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        ChecksumData<String> config = findFromDisk(configId);
        if (config != null) {
            return config;
        }

        Monitor.notFoundConfigFileFromDiskCounter.inc();
        log.warn("config not found from disk: {}", configId);
        config = findFromDb(configId);
        if (config != null) {
            return config;
        }
        Monitor.notFoundConfigFileFromDbCounter.inc();

        throw new ConfigNotFoundException();
    }

    private void saveToFile(VersionData<ConfigMeta> configId, ChecksumData<String> config) {
        try {
            fileConfigDao.save(configId, config);
        } catch (FileDaoProcessException e) {
            Monitor.syncConfigFileFailCounter.inc();
            log.error("write config to file error. {}", e.getConfigId(), e);
        }
    }

    private ChecksumData<String> findFromDb(VersionData<ConfigMeta> configId) {
        ChecksumData<String> config = configDao.loadFromCandidateSnapshot(configId);
        if (config != null) {
            saveToFile(configId, config);
        }
        return config;
    }

    private ChecksumData<String> findFromDisk(VersionData<ConfigMeta> configId) {
        try {
            return fileConfigDao.find(configId);
        } catch (FileDaoProcessException e) {
            log.error("read config file error. {}", e.getConfigId());
        } catch (ChecksumFailedException e) {
            log.error("checksum failed, {}", configId, e);
            Monitor.checkSumFailCounter.inc();
        }
        return null;
    }

    @Override
    public VersionData<ChecksumData<String>> forceLoad(String ip, VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        // 无效ip无须进行版本锁定检查
        if (!AddressUtil.INVALID_IP.equals(ip)) {
            VersionData<ChecksumData<String>> fixedVersionData = forceloadWithFixedVersion(configId, ip);
            if (fixedVersionData != null) {
                return fixedVersionData;
            }
        }

        VersionData<ChecksumData<String>> cacheData = forceloadFromCache(ip, configId);
        if (cacheData != null) {
            return cacheData;
        }

        Monitor.notFoundConfigFileFromDiskCounter.inc();
        VersionData<ChecksumData<String>> dbData = forceloadFromDb(ip, configId);
        if (dbData != null) {
            return dbData;
        }
        Monitor.notFoundConfigFileFromDbCounter.inc();

        throw new ConfigNotFoundException();
    }

    private VersionData<ChecksumData<String>> forceloadFromDb(String ip, VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        Optional<Long> version = dbConfigInfoService.getVersion(configId.getData(), ip);
        return doForceLoad(configId, version);
    }

    private VersionData<ChecksumData<String>> doForceLoad(VersionData<ConfigMeta> configId, Optional<Long> version) throws ConfigNotFoundException {
        if (version.isPresent() && version.get() >= configId.getVersion()) {
            return VersionData.of(version.get(), findConfig(VersionData.of(version.get(), configId.getData())));
        } else {
            return null;
        }
    }

    private VersionData<ChecksumData<String>> forceloadFromCache(String ip, VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        Optional<Long> version = cacheConfigInfoService.getVersion(configId.getData(), ip);
        return doForceLoad(configId, version);
    }

    private VersionData<ChecksumData<String>> forceloadWithFixedVersion(VersionData<ConfigMeta> configId, String ip) throws ConfigNotFoundException {
        Monitor.forceLoadFixedVersionCounter.inc();
        Optional<Long> fixedVersion = cacheFixedVersionConsumerService.getFixedVersion(configId.getData(), ip);
        if (fixedVersion.isPresent()) {
            if (fixedVersion.get() < configId.getVersion()) {
                throw new ConfigNotFoundException();
            }
            return VersionData.of(fixedVersion.get(), findConfig(VersionData.of(fixedVersion.get(), configId.getData())));
        }
        return null;
    }

    @Override
    public void update(ConfigMeta configMeta) {
        VersionData<ConfigMeta> configId = configDao.load(configMeta);
        if (configId == null) {
            cacheConfigVersionService.update(VersionData.of(0, configMeta), UpdateType.DELETE);
            return;
        }

        // todo: 这个东西放到这里不太合力，不过得仔细看看admin那边一起改
        Optional<ConfigMeta> parentConfigMeta = configDao.loadReference(configMeta, RefType.INHERIT);
        if(parentConfigMeta.isPresent()) {
            cacheService.updateReferenceCache(new ReferenceInfo(configMeta, parentConfigMeta.get(), RefType.INHERIT), RefChangeType.ADD);
        }

        // 上一个版本：先通知客户端，再做存盘操作
        // 但server端比较慢的时候，会出现大量客户端直接查询db
        // 因此还是先存盘，再通知
        ChecksumData<String> config = configDao.loadFromCandidateSnapshot(configId);
        saveToFile(configId, config);

        cacheConfigVersionService.update(configId, UpdateType.UPDATE);
    }

    /**
     * 加载子环境和环境的配置文件信息，子环境的文件优先级高于环境。
     * 为了减少文件数量，不加载resource的配置文件，因为是可扩展的，所以暂时不考虑resource的配置文件
     * @param group
     * @param profile
     * @return
     */
    @Override
    public List<VersionData<ConfigMeta>> loadByGroupAndProfile(String group, String profile) {
        List<VersionData<ConfigMeta>> filesInCurrentProfile = configDao.loadByGroupAndProfile(group, profile);

        String defaultProfile = Environment.extractDefaultProfile(profile).defaultProfile();
        if (Objects.equal(defaultProfile, profile)) {
            return filesInCurrentProfile;
        }

        List<VersionData<ConfigMeta>> filesInDefaultProfile = configDao.loadByGroupAndProfile(group, defaultProfile);
        return merge(filesInCurrentProfile, filesInDefaultProfile);
    }

    private List<VersionData<ConfigMeta>> merge(List<VersionData<ConfigMeta>> filesInCurrentProfile, List<VersionData<ConfigMeta>> filesInDefaultProfile) {
        Map<String, VersionData<ConfigMeta>> result = Maps.newHashMap();
        for (VersionData<ConfigMeta> fileInDefaultProfile : filesInDefaultProfile) {
            result.put(fileInDefaultProfile.getData().getDataId(), fileInDefaultProfile);
        }
        for (VersionData<ConfigMeta> fileInCurrentProfile : filesInCurrentProfile) {
            result.put(fileInCurrentProfile.getData().getDataId(), fileInCurrentProfile);
        }
        return ImmutableList.copyOf(result.values());
    }
}
