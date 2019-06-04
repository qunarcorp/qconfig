package qunar.tc.qconfig.admin.model;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * 继承文件Meta
 * Created by chenjk on 2017/4/1.
 */
public class InheritConfigMeta {

    //应用名称
    private String groupId;

    //文件名称
    private String dataId;

    //文件环境
    private String profile;

    private String inheritGroupId;

    private String inheritDataId;

    private String inheritProfile;

    private String operator;

    private int status;

    private Date createTime;

    public InheritConfigMeta() {

    }

    public InheritConfigMeta(String groupId, String dataId, String profile) {
        this.groupId = groupId;
        this.dataId = dataId;
        this.profile = profile;
    }

    public InheritConfigMeta(String groupId, String dataId) {
        this.groupId = groupId;
        this.dataId = dataId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public String getInheritGroupId() {
        return inheritGroupId;
    }

    public void setInheritGroupId(String inheritGroupId) {
        this.inheritGroupId = inheritGroupId;
    }

    public String getInheritDataId() {
        return inheritDataId;
    }

    public void setInheritDataId(String inheritDataId) {
        this.inheritDataId = inheritDataId;
    }

    public String getInheritProfile() {
        return inheritProfile;
    }

    public void setInheritProfile(String inheritProfile) {
        this.inheritProfile = inheritProfile;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 判断当前文件是否支持被继承
     *
     * @return
     */
    public boolean isValidInheritConfig() {
        return (StringUtils.isNotBlank(getGroupId())
                && StringUtils.isNotBlank(getDataId())
                && getDataId().endsWith(".properties"));
    }

}
