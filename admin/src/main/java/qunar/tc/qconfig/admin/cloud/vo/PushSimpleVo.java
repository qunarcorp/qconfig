package qunar.tc.qconfig.admin.cloud.vo;

public class PushSimpleVo {

    private String group;

    private String dataId;

    private String profile;

    private long basedVersion = 0;

    private long editVersion = 0;

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

    public long getBasedVersion() {
        return basedVersion;
    }

    public void setBasedVersion(long basedVersion) {
        this.basedVersion = basedVersion;
    }

    public long getEditVersion() {
        return editVersion;
    }

    public void setEditVersion(long editVersion) {
        this.editVersion = editVersion;
    }

    @Override
    public String toString() {
        return "PushSimpleVo{" + "group='" + group + '\'' + ", dataId='" + dataId + '\'' + ", profile='" + profile
                + '\'' + ", basedVersion=" + basedVersion + ", editVersion=" + editVersion + '}';
    }
}
