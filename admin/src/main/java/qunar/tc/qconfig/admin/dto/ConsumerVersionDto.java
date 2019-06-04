package qunar.tc.qconfig.admin.dto;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
public class ConsumerVersionDto {

    private String group;

    private String dataId;

    private String profile;

    private String ip;

    private long version;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ConsumerVersionDto{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", ip='" + ip + '\'' +
                ", version=" + version +
                '}';
    }
}
