package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/7/3 18:55
 */
public interface CacheService {

    void updateReferenceCache(ReferenceInfo referenceInfo, RefChangeType changeType);

    Optional<ConfigMeta> getReference(ConfigMeta configMeta);

    Optional<ConfigMeta> getParent(ConfigMeta childFile);

    Set<ConfigMeta> getChildren(ConfigMeta parent);
}
