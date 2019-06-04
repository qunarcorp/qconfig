package qunar.tc.qconfig.server.config.qfile.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.config.impl.CacheConfigInfoService;
import qunar.tc.qconfig.server.config.impl.DbConfigInfoService;
import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * 继承文件实体
 * <p>
 * Created by chenjk on 2017/5/18.
 */
public class InheritQFileV2 extends AbstractQFileEntity {

    private static final Logger logger = LoggerFactory.getLogger(InheritQFileV2.class);

    private static final Joiner JOINER = Joiner.on("=");

    private QFile childFile;

    private QFile parentFile;

    private DbConfigInfoService dbConfigInfoService;

    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    public InheritQFileV2(ConfigMeta meta,
                          QFile childFile,
                          QFile parentFile,
                          ConfigStore configStore,
                          LogService logService,
                          ClientInfoService clientInfoService,
                          CacheConfigInfoService cacheConfigInfoService,
                          DbConfigInfoService dbConfigInfoService,
                          CacheFixedVersionConsumerService cacheFixedVersionConsumerService) {
        super(meta, cacheConfigInfoService, configStore, logService, clientInfoService);
        this.childFile = childFile;
        this.parentFile = parentFile;
        this.dbConfigInfoService = dbConfigInfoService;
        this.cacheFixedVersionConsumerService = cacheFixedVersionConsumerService;
    }

    @Override
    public Optional<Changed> checkChange(CheckRequest request, String ip) {
        Optional<Long> childVersion = getCacheConfigInfoService().getVersion(childFile.getSourceMeta(), ip);
        Optional<Long> parentVersion = getCacheConfigInfoService().getVersion(parentFile.getSourceMeta(), ip);
        if (!childVersion.isPresent() || !parentVersion.isPresent()) {
            return Optional.absent();
        } else {
            long finalVersion = childVersion.get() + parentVersion.get();
            if (finalVersion <= request.getVersion()
                    && request.getLoadProfile().equalsIgnoreCase(childFile.getSourceMeta().getProfile())) {//版本更新，或者profile变了这种情况，可能是子文件在resources下继承了，之后又在uat环境继承
                return Optional.absent();
            } else {
                Monitor.inheritFixedVersionCounter.inc(2);
                Optional<Long> childFixedVersion = cacheFixedVersionConsumerService.getFixedVersion(childFile.getSourceMeta(), ip);
                Optional<Long> parentFixedVersion = cacheFixedVersionConsumerService.getFixedVersion(parentFile.getSourceMeta(), ip);
                if (childFixedVersion.isPresent() && parentFixedVersion.isPresent()) {//版本锁定
                    long fixedVersion = childFixedVersion.get() + parentFixedVersion.get();
                    if (fixedVersion <= request.getVersion()) {
                        return Optional.absent();
                    }
                }
                return Optional.of(new Changed(parentFile.getSourceMeta().getGroup(), childFile.getSourceMeta().getDataId(),
                        childFile.getSourceMeta().getProfile(), finalVersion));
            }
        }

    }

    @Override
    public ChecksumData<String> findConfig(long version) throws ConfigNotFoundException {
        Optional<Long> childVersion = getCacheConfigInfoService().getVersion(childFile.getSourceMeta(), clientInfoService.getIp());
        Optional<Long> parentVersion = getCacheConfigInfoService().getVersion(parentFile.getSourceMeta(), clientInfoService.getIp());
        if (childVersion.isPresent()
                && parentVersion.isPresent()
                && (childVersion.get() + parentVersion.get() == version)) {
            ChecksumData<String> childData = getConfigStore().findConfig(VersionData.of(childVersion.get(), childFile.getSourceMeta()));
            ChecksumData<String> parentData = getConfigStore().findConfig(VersionData.of(parentVersion.get(), parentFile.getSourceMeta()));
            String resultStr = merge(childData.getData(), parentData.getData());
            return ChecksumData.of(ChecksumAlgorithm.getChecksum(resultStr), resultStr);
        } else {
            logger.error("findConfig未找到父子文件版本信息，meta[{}], 父[{}]， 子[{}]", getSourceMeta(), parentVersion, childVersion);
            throw new ConfigNotFoundException();
        }

    }

    @Override
    public VersionData<ChecksumData<String>> forceLoad(String ip, long version) throws ConfigNotFoundException {
        VersionData<ChecksumData<String>> childData = getConfigStore().forceLoad(ip, VersionData.of(version, childFile.getSourceMeta()));
        VersionData<ChecksumData<String>> parentData = getConfigStore().forceLoad(ip, VersionData.of(version, parentFile.getSourceMeta()));
        String resultStr = merge(childData.getData().getData(), parentData.getData().getData());
        if (version > childData.getVersion() + parentData.getVersion()) {
            logger.warn("forceLoad的版本号[{}]不符合预期, meta[{}]", version, getSourceMeta());
            throw new ConfigNotFoundException();
        } else {
            return VersionData.of(childData.getVersion() + parentData.getVersion(), ChecksumData.of(ChecksumAlgorithm.getChecksum(resultStr), resultStr));
        }
    }

    @Override
    public Listener createListener(CheckRequest request, AsyncContextHolder contextHolder) {
        return new InheritListener(childFile.getSourceMeta(), contextHolder, request.getVersion());
    }

    public CacheConfigInfoService getCacheConfigInfoService() {
        return super.getCacheConfigInfoService();
    }

    private String merge(String childConfiguration, String parentConfiguration) {
        childConfiguration = childConfiguration == null ? "" : childConfiguration;
        parentConfiguration = parentConfiguration == null ? "" : parentConfiguration;

        try {
            Properties childProperties = new Properties();
            childProperties.load(new StringReader(childConfiguration));
            Properties parentProperties = new Properties();
            parentProperties.load(new StringReader(parentConfiguration));
            //merge
            for (Map.Entry<Object, Object> entry : childProperties.entrySet()) {
                parentProperties.put(entry.getKey(), entry.getValue());
            }
            return parseProperties2String(parentProperties);
        } catch (IOException e) {
            logger.error("parse configuration to properties error.", e);
            return null;
        }
    }

    private String parseProperties2String(Properties properties) {
        if (properties == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            result.append(JOINER.join(entry.getKey(), entry.getValue())).append("\n");
        }
        return result.toString();
    }

    public QFile getChildFile() {
        return childFile;
    }

    public QFile getParentFile() {
        return parentFile;
    }

    public DbConfigInfoService getDbConfigInfoService() {
        return dbConfigInfoService;
    }

    @Override
    public ConfigMeta getSharedMeta() {
        return childFile.getSourceMeta();
    }

    @Override
    public ConfigMeta getRealMeta() {
        return childFile.getSourceMeta();
    }

    @Override
    public void log(Log log) {
        Optional<Long> childVersion = getCacheConfigInfoService().getVersion(getChildFile().getRealMeta());
        Optional<Long> parentVersion = getCacheConfigInfoService().getVersion(getParentFile().getRealMeta());
        if (childVersion.isPresent()) {
            Log childLog = new Log(getChildFile().getRealMeta().getGroup(), getChildFile().getRealMeta().getDataId(), getChildFile().getRealMeta().getProfile(), childVersion.get(), log.getIp(), log.getPort(), log.getType(), log.getText());
            getChildFile().log(childLog);
        }
        if (parentVersion.isPresent()) {
            Log parentLog = new Log(getParentFile().getRealMeta().getGroup(), getParentFile().getRealMeta().getDataId(), getParentFile().getRealMeta().getProfile(), parentVersion.get(), log.getIp(), log.getPort(), log.getType(), log.getText());
            getParentFile().log(parentLog);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "sourceMeta=" + getSourceMeta() +
                ", parentFile=" + getParentFile() +
                ", childFile=" + getChildFile() +
                '}';
    }
}