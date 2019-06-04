package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.MetaIp;

public interface CacheFixedVersionConsumerService {

    void update(MetaIp consumer, long version);

    Optional<Long> getFixedVersion(ConfigMeta meta, String ip);
}
