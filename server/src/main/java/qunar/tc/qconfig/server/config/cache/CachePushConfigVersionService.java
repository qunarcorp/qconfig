package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PushConfigVersionItem;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 12:23
 */
public interface CachePushConfigVersionService {

    Optional<Long> getVersion(ConfigMeta meta, String ip);

    void update(PushConfigVersionItem item);
}
