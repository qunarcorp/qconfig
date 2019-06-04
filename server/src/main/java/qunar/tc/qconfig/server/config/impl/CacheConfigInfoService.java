package qunar.tc.qconfig.server.config.impl;

import com.google.common.base.Optional;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.cache.CacheConfigVersionService;
import qunar.tc.qconfig.server.config.cache.CachePushConfigVersionService;
import qunar.tc.qconfig.server.config.cache.CacheService;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/10/28 20:40
 */
@Service("cacheConfigInfoService")
public class CacheConfigInfoService implements ConfigInfoService {

    @Resource
    private CacheService cacheService;

    @Resource
    private CacheConfigVersionService cacheConfigVersionService;

    @Resource
    private CachePushConfigVersionService cachePushConfigVersionService;

    @Override
    public Optional<Long> getVersion(ConfigMeta meta) {
        return cacheConfigVersionService.getVersion(meta);
    }

    @Override
    public Optional<Long> getPushVersion(ConfigMeta meta, String ip) {
        return cachePushConfigVersionService.getVersion(meta, ip);
    }

    @Override
    public Optional<Long> getVersion(ConfigMeta meta, String ip) {
        Optional<Long> publishVersion = getVersion(meta);
        Optional<Long> pushVersion = getPushVersion(meta, ip);
        return VersionUtil.getLoadVersion(publishVersion, pushVersion);
    }

    @Override
    public Optional<ConfigMeta> getReference(ConfigMeta meta) {
        return cacheService.getReference(meta);
    }

    @Override
    public Optional<ConfigMeta> getParent(ConfigMeta child) {
        if (getVersion(child).isPresent()) {
            return cacheService.getParent(child);
        }
        return Optional.absent();
    }

    @Override
    public Set<ConfigMeta> getChildren(ConfigMeta parent) {
        return cacheService.getChildren(parent);
    }
}
