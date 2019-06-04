package qunar.tc.qconfig.admin.cloud.vo;

public class PropertiesEntryVo {

    private String groupId;
    private String groupName;
    private String profile;
    private String dataId;
    private String key;
    private String value;
    private long version;

    public PropertiesEntryVo() {
    }

    public PropertiesEntryVo(String groupId, String groupName, String profile, String dataId, String key, String value, long version) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.profile = profile;
        this.dataId = dataId;
        this.key = key;
        this.value = value;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "PropertiesEntryVo{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", profile='" + profile + '\'' +
                ", dataId='" + dataId + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", version=" + version +
                '}';
    }
}
