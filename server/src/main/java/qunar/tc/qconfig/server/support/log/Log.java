package qunar.tc.qconfig.server.support.log;

import qunar.tc.qconfig.common.util.ConfigLogType;

/**
 * User: zhaohuiyu
 * Date: 5/21/14
 * Time: 4:58 PM
 */
public class Log {

    private String group;

    private String dataId;

    private String profile;

    private long version;

    private String ip;

    private int port;

    private ConfigLogType type;

    private String text;

    public Log() {

    }

    public Log(String group, String dataId, String profile, long version, String ip, int port, String text) {
        this(group, dataId, profile, version, ip, port, ConfigLogType.PULL_SUCCESS, text);
    }

    // todo: 参数太多了
    public Log(String group, String dataId, String profile, long version, String ip, int port, ConfigLogType type, String text) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.version = version;
        this.ip = ip;
        this.port = port;
        this.type = type;
        this.text = text;
    }

    public String getGroup() {
        return group;
    }

    public String getDataId() {
        return dataId;
    }

    public String getProfile() {
        return profile;
    }

    public long getVersion() {
        return version;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public ConfigLogType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setType(ConfigLogType type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Log{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", version=" + version +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", type=" + type +
                ", text='" + text + '\'' +
                '}';
    }
}
