package qunar.tc.qconfig.servercommon.bean;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:13
 */
public class ConfigMetaWithoutProfile {

    private final String group;

    private final String dataId;

    public ConfigMetaWithoutProfile(String group, String dataId) {
        this.group = group;
        this.dataId = dataId;
    }

    public ConfigMetaWithoutProfile(ConfigMeta meta) {
        this.group = meta.getGroup();
        this.dataId= meta.getDataId();
    }

    public String getGroup() {
        return group;
    }

    public String getDataId() {
        return dataId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigMetaWithoutProfile that = (ConfigMetaWithoutProfile) o;

        if (getGroup() != null ? !getGroup().equalsIgnoreCase(that.getGroup()) : that.getGroup() != null) return false;
        return getDataId() != null ? getDataId().equalsIgnoreCase(that.getDataId()) : that.getDataId() == null;

    }

    @Override
    public int hashCode() {
        int result = getGroup() != null ? getGroup().toLowerCase().hashCode() : 0;
        result = 31 * result + (getDataId() != null ? getDataId().toLowerCase().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigMetaWithoutProfile{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                '}';
    }
}