package qunar.tc.qconfig.admin.web.bean;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 6:14 PM
 */
public class ConfigField {

    private String group;

    private String dataId;

    private String profile;

    public ConfigField() {
    }

    public ConfigField(String group, String dataId, String profile) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigField)) return false;

        ConfigField meta = (ConfigField) o;

        if (dataId != null ? !dataId.equalsIgnoreCase(meta.dataId) : meta.dataId != null) return false;
        if (group != null ? !group.equalsIgnoreCase(meta.group) : meta.group != null) return false;
        if (profile != null ? !profile.equalsIgnoreCase(meta.profile) : meta.profile != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.toLowerCase().hashCode() : 0;
        result = 31 * result + (dataId != null ? dataId.toLowerCase().hashCode() : 0);
        result = 31 * result + (profile != null ? profile.toLowerCase().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigField{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                '}';
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
}
