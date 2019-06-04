package qunar.tc.qconfig.server.config.impl;

import com.google.common.base.Optional;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.server.config.cache.CacheConfigVersionService;
import qunar.tc.qconfig.server.config.cache.CachePushConfigVersionService;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.dao.PushConfigVersionDao;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.server.domain.UpdateType;
import qunar.tc.qconfig.server.config.cache.CacheService;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PushConfigVersionItem;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 11:14
 */
@Service("dbConfigInfoService")
public class DbConfigInfoService implements ConfigInfoService {

    @Resource
    private CacheService cacheService;

    @Resource
    private CacheConfigVersionService cacheConfigVersionService;

    @Resource
    private CachePushConfigVersionService cachePushConfigVersionService;

    @Resource
    private ConfigDao configDao;

    @Resource
    private PushConfigVersionDao pushConfigVersionDao;

    @Override
    public Optional<Long> getVersion(ConfigMeta meta) {
        VersionData<ConfigMeta> versionData = configDao.load(meta);
        if (versionData != null) {
            cacheConfigVersionService.update(versionData, UpdateType.UPDATE);
            return Optional.of(versionData.getVersion());
        }

        return Optional.absent();
    }

    @Override
    public Optional<Long> getPushVersion(ConfigMeta meta, String ip) {
        PushConfigVersionItem item = pushConfigVersionDao.select(meta, ip);
        if (item != null) {
            cachePushConfigVersionService.update(item);
            return Optional.of(item.getVersion());
        }
        return Optional.absent();
    }

    @Override
    public Optional<Long> getVersion(ConfigMeta meta, String ip) {
        Optional<Long> publishVersion = getVersion(meta);
        Optional<Long> pushVersion = getPushVersion(meta, ip);
        return VersionUtil.getLoadVersion(publishVersion, pushVersion);
    }

    @Override
    public Optional<ConfigMeta> getReference(ConfigMeta meta) {
        return loadTargetMeta(meta, RefType.REFERENCE);
    }

    @Override
    public Optional<ConfigMeta> getParent(ConfigMeta child) {
        if (getVersion(child).isPresent()) {
            return loadTargetMeta(child, RefType.INHERIT);
        }
        return Optional.absent();
    }

    @Override
    public Set<ConfigMeta> getChildren(ConfigMeta parent) {
        // 继承实现的不对，还要改实现，这里就先只实现cache不实现db
        throw new UnsupportedOperationException();
    }

    private Optional<ConfigMeta> loadTargetMeta(ConfigMeta meta, RefType reftype) {
        Optional<ConfigMeta> targetMeta = configDao.loadReference(meta, reftype);
        if (targetMeta.isPresent()) {
            cacheService.updateReferenceCache(new ReferenceInfo(meta, targetMeta.get(), reftype), RefChangeType.ADD);
            return Optional.of(targetMeta.get());
        }
        return Optional.absent();
    }
}
