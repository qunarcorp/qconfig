package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.impl.CacheConfigInfoService;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * @author zhenyu.nie created on 2014 2014/11/3 16:32
 */
public abstract class AbstractQFileEntity extends AbstractQFile implements QFile {

    private ConfigMeta meta;

    private CacheConfigInfoService cacheConfigInfoService;

    private ConfigStore configStore;

    private LogService logService;

    protected ClientInfoService clientInfoService;

    public AbstractQFileEntity(ConfigMeta meta,
                               CacheConfigInfoService cacheConfigInfoService,
                               ConfigStore configStore,
                               LogService logService,
                               ClientInfoService clientInfoService) {
        this.meta = meta;
        this.cacheConfigInfoService = cacheConfigInfoService;
        this.configStore = configStore;
        this.logService = logService;
        this.clientInfoService = clientInfoService;
    }

    @Override
    public ConfigMeta getSourceMeta() {
        return meta;
    }

    @Override
    public ConfigMeta getSharedMeta() {
        return meta;
    }

    @Override
    public ConfigMeta getRealMeta() {
        return meta;
    }

    @Override
    public ChecksumData<String> findConfig(long version) throws ConfigNotFoundException {
        return configStore.findConfig(VersionData.of(version, meta));
    }

    @Override
    public VersionData<ChecksumData<String>> forceLoad(String ip, long version) throws ConfigNotFoundException {
        return configStore.forceLoad(ip, VersionData.of(version, meta));
    }

    protected CacheConfigInfoService getCacheConfigInfoService() {
        return cacheConfigInfoService;
    }

    protected ConfigStore getConfigStore() {
        return configStore;
    }

    @Override
    public LogService getLogService() {
        return logService;
    }
}
