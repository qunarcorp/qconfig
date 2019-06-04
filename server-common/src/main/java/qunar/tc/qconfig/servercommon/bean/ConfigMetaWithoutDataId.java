package qunar.tc.qconfig.servercommon.bean;

/**
 * @author zhenyu.nie created on 2018 2018/1/29 12:19
 */
public class ConfigMetaWithoutDataId {

    private final String group;

    private final String profile;

    public ConfigMetaWithoutDataId(String group, String profile) {
        this.group = group;
        this.profile = profile;
    }

    public String getGroup() {
        return group;
    }

    public String getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigMetaWithoutDataId that = (ConfigMetaWithoutDataId) o;

        if (group != null ? !group.equalsIgnoreCase(that.group) : that.group != null) return false;
        return profile != null ? profile.equalsIgnoreCase(that.profile) : that.profile == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.toLowerCase().hashCode() : 0;
        result = 31 * result + (profile != null ? profile.toLowerCase().hashCode() : 0);
        return result;
    }
}
