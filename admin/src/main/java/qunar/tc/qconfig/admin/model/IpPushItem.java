package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 11:42
 */
public class IpPushItem {

    private Host host;

    private ConfigMeta meta;

    public IpPushItem(Host host, ConfigMeta meta) {
        this.host = host;
        this.meta = meta;
    }

    public Host getHost() {
        return host;
    }

    public ConfigMeta getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return "IpPushItem{" +
                "host=" + host +
                ", meta=" + meta +
                '}';
    }
}
