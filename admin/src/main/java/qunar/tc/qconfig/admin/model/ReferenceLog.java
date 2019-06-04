package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import java.sql.Timestamp;

/**
 * Date: 14-7-7 Time: 下午4:47
 * 
 * @author: xiao.liang
 * @description:
 */
public class ReferenceLog {

    private String group;

    private String profile;

    private String alias;

    private String refGroup;

    private String refProfile;

    private String refDataId;

    private String operator;

    private RefChangeType changeType;

    private Timestamp createTime;

    public ReferenceLog() {
    }

    public ReferenceLog(Reference reference, RefChangeType changeType) {
        this(reference.getGroup(), reference.getProfile(), reference.getAlias(), reference.getRefGroup(), reference
                .getRefProfile(), reference.getRefDataId(), reference.getOperator(), changeType, null);
    }

    public ReferenceLog(String group, String profile, String alias, String refGroup, String refProfile,
            String refDataId, String operator, RefChangeType changeType, Timestamp createTime) {
        this.group = group;
        this.profile = profile;
        this.alias = alias;
        this.refGroup = refGroup;
        this.refProfile = refProfile;
        this.refDataId = refDataId;
        this.operator = operator;
        this.changeType = changeType;
        this.createTime = createTime;
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

    public RefChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(RefChangeType changeType) {
        this.changeType = changeType;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
