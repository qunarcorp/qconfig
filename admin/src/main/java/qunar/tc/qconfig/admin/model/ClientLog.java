package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.support.HostnameUtil;
import qunar.tc.qconfig.common.util.ConfigLogType;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/6/9 17:26
 */
public class ClientLog {

    private String ip;

    private String hostname;

    private long basedVersion;

    private long version;

    private ConfigLogType type;

    private String remarks;

    private Timestamp time;

    public ClientLog(String ip, long basedVersion, long version, ConfigLogType type, String remarks, Timestamp time) {
        hostname = HostnameUtil.getHostnameFromIp(ip);
        this.ip = ip;
        this.basedVersion = basedVersion;
        this.version = version;
        this.type = type;
        this.remarks = remarks;
        this.time = time;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public long getBasedVersion() {
        return basedVersion;
    }

    public long getVersion() {
        return version;
    }

    public ConfigLogType getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getTypeText() {
        return getType().getText();
    }

    public Timestamp getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "ClientLog{" +
                "ip='" + ip + '\'' +
                ", hostname='" + hostname + '\'' +
                ", basedVersion=" + basedVersion +
                ", version=" + version +
                ", type=" + type +
                ", remarks='" + remarks + '\'' +
                ", time=" + time +
                '}';
    }
}
