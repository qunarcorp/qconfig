package qunar.tc.qconfig.server.config.longpolling;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 18:01
 */
public interface LongPollingStore {

    void addListener(Listener listener);

    void manualPush(ConfigMeta meta, long version, Set<IpAndPort> ipAndPorts);

    void manualPushIps(ConfigMeta meta, long version, Set<String> ips);

    void onChange(ConfigMeta meta, long version);
}
