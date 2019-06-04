package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * Date: 14-6-30
 * Time: 下午2:47
 *
 * @author: xiao.liang
 * @description:
 */
public class Reference {

    private long id;

    private String group;

    private String profile;

    private String alias;

    private String refGroup;

    private String refProfile;

    private String refDataId;

    private String operator;

    private int type;

    private Timestamp createTime;

    public Reference() {

    }

    public Reference(String group, String profile, String alias, String refGroup, String refProfile, String refDataId, String operator, Timestamp createTime) {
        this.group = group;
        this.profile = profile;
        this.alias = alias;
        this.refGroup = refGroup;
        this.refProfile = refProfile;
        this.refDataId = refDataId;
        this.operator = operator;
        this.createTime = createTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reference)) return false;

        Reference reference = (Reference) o;

        if (type != reference.type) return false;
        if (group != null ? !group.equalsIgnoreCase(reference.group) : reference.group != null) return false;
        if (alias != null ? !alias.equalsIgnoreCase(reference.alias) : reference.alias != null) return false;
        if (profile != null ? !profile.equalsIgnoreCase(reference.profile) : reference.profile != null) return false;
        if (refGroup != null ? !refGroup.equalsIgnoreCase(reference.refGroup) : reference.refGroup != null) return false;
        if (refDataId != null ? !refDataId.equalsIgnoreCase(reference.refDataId) : reference.refDataId != null) return false;
        if (refProfile != null ? !refProfile.equalsIgnoreCase(reference.refProfile) : reference.refProfile != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.toLowerCase().hashCode() : 0;
        result = 31 * result + (alias != null ? alias.toLowerCase().hashCode() : 0);
        result = 31 * result + (profile != null ? profile.toLowerCase().hashCode() : 0);
        result = 31 * result + (refGroup != null ? profile.toLowerCase().hashCode() : 0);
        result = 31 * result + (refDataId != null ? profile.toLowerCase().hashCode() : 0);
        result = 31 * result + (refProfile != null ? profile.toLowerCase().hashCode() : 0);
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "Reference{" +
                "group='" + group + '\'' +
                ", profile='" + profile + '\'' +
                ", alias='" + alias + '\'' +
                ", refGroup='" + refGroup + '\'' +
                ", refProfile='" + refProfile + '\'' +
                ", refDataId='" + refDataId + '\'' +
                ", operator='" + operator + '\'' +
                ", type=" + type +
                ", createTime=" + createTime +
                '}';
    }
}
