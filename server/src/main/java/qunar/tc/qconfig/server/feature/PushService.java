package qunar.tc.qconfig.server.feature;

import qunar.tc.qconfig.servercommon.bean.IpAndPort;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2017 2017/4/5 19:16
 */
public interface PushService {

    void push(ConfigMeta meta, long version, Set<IpAndPort> ipAndPorts);

    void pushWithIp(ConfigMeta meta, long version, Set<String> ips);
}
