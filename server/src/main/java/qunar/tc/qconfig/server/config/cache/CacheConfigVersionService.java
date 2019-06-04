package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.domain.UpdateType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 14:33
 */
public interface CacheConfigVersionService {

    Optional<Long> getVersion(ConfigMeta meta);

    void update(VersionData<ConfigMeta> configId, UpdateType updateType);
}
