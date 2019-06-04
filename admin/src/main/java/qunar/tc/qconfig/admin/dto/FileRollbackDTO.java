package qunar.tc.qconfig.admin.dto;

/**
 * @author pingyang.yang
 * @since 2018/9/28
 */
public class FileRollbackDTO {

    private String group;

    private String dataId;

    private String profile;

    private int currentVersion;

    private int targetVersion;

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

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public int getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(int targetVersion) {
        this.targetVersion = targetVersion;
    }
}
