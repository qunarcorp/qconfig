package qunar.tc.qconfig.server.domain;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
public class IpAndVersion {

    private final String ip;

    private final long version;

    public IpAndVersion(String ip, long version) {
        this.ip = ip;
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "IpAndVersion{" +
                "ip='" + ip + '\'' +
                ", version=" + version +
                '}';
    }
}
