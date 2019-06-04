package qunar.tc.qconfig.server.dao;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.MetaIp;

import java.util.Map;

/**
 * @author yunfeng.yang
 * @since 2017/5/15
 */
public interface FixedConsumerVersionDao {

    Long find(ConfigMeta meta, String ip);

    Map<MetaIp, Long> queryAll();
}
