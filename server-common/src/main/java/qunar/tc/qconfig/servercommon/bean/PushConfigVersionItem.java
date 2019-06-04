package qunar.tc.qconfig.servercommon.bean;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 12:15
 */
public class PushConfigVersionItem {

    private final ConfigMeta meta;

    private final String ip;

    private final long version;

    public PushConfigVersionItem(ConfigMeta meta, String ip, long version) {
        this.meta = meta;
        this.ip = ip;
        this.version = version;
    }

    public ConfigMeta getMeta() {
        return meta;
    }

    public String getIp() {
        return ip;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "PushConfigVersion{" +
                "meta=" + meta +
                ", ip='" + ip + '\'' +
                ", version=" + version +
                '}';
    }
}
