package qunar.tc.qconfig.admin.dto;

/**
 * Date: 14-7-3
 * Time: 上午10:34
 *
 * @author: xiao.liang
 * @description:
 */
public class ReferenceConfigDTO {

    private String group;

    private String alias;

    private String profile;

    private String refGroup;

    private String refProfile;

    private String refDataId;

    private long refEditVersion = 0;

    public ReferenceConfigDTO() {
    }

    public ReferenceConfigDTO(String group, String alias, String profile, String refGroup, String refProfile, String refDataId, long refEditVersion) {
        this.group = group;
        this.alias = alias;
        this.profile = profile;
        this.refGroup = refGroup;
        this.refProfile = refProfile;
        this.refDataId = refDataId;
        this.refEditVersion = refEditVersion;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getRefGroup() {
        return refGroup;
    }

    public void setRefGroup(String refGroup) {
        this.refGroup = refGroup;
    }

    public String getRefProfile() {
        return refProfile;
    }

    public void setRefProfile(String refProfile) {
        this.refProfile = refProfile;
    }

    public String getRefDataId() {
        return refDataId;
    }

    public void setRefDataId(String refDataId) {
        this.refDataId = refDataId;
    }

    public long getRefEditVersion() {
        return refEditVersion;
    }

    public void setRefEditVersion(long refEditVersion) {
        this.refEditVersion = refEditVersion;
    }

}
