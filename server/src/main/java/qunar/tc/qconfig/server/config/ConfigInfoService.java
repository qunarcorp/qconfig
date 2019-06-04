package qunar.tc.qconfig.server.config;

import com.google.common.base.Optional;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/10/28 20:39
 */
public interface ConfigInfoService {

    Optional<Long> getVersion(ConfigMeta meta);

    Optional<Long> getPushVersion(ConfigMeta meta, String ip);

    Optional<Long> getVersion(ConfigMeta meta, String ip);

    Optional<ConfigMeta> getReference(ConfigMeta meta);

    Optional<ConfigMeta> getParent(ConfigMeta child);

    Set<ConfigMeta> getChildren(ConfigMeta parent);
}
