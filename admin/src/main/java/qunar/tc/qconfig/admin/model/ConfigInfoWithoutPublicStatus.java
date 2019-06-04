package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2016 2016/5/5 17:27
 */
public class ConfigInfoWithoutPublicStatus {
    private String group;
    private String dataId;
    private String profile;
    private long version;
    private boolean inuse;

    private InheritConfigMeta inheritConfigMeta;

    private Timestamp updateTime;

    public ConfigInfoWithoutPublicStatus(String group, String dataId, String profile, long version, boolean publish, Timestamp updateTime) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.version = version;
        this.inuse = publish;
        this.updateTime = updateTime;
    }

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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isInuse() {
        return inuse;
    }

    public void setInuse(boolean inuse) {
        this.inuse = inuse;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public InheritConfigMeta getInheritConfigMeta() {
        return inheritConfigMeta;
    }

    public void setInheritConfigMeta(InheritConfigMeta inheritConfigMeta) {
        this.inheritConfigMeta = inheritConfigMeta;
    }

    @Override
    public String toString() {
        return "ConfigInfoWithoutPublicStatus{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", version=" + version +
                ", inuse=" + inuse +
                ", updateTime=" + updateTime +
                '}';
    }
}
