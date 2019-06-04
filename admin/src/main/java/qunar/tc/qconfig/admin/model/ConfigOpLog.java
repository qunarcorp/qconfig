package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.event.ConfigOperationEvent;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 21:38
 */
public class ConfigOpLog {

    private String group;

    private String dataId;

    private String profile;

    private long basedVersion;

    private long editVersion;

    private String operator;

    private ConfigOperationEvent operationType;

    private String remarks;

    private String ip;

    private Timestamp operationTime;

    public ConfigOpLog() {
    }

    public ConfigOpLog(String group, String dataId, String profile, long basedVersion, long editVersion,
                       String operator, ConfigOperationEvent operationType, String remarks, String ip) {
        this(group, dataId, profile, basedVersion, editVersion, operator, operationType, remarks, ip, null);
    }

    public ConfigOpLog(String group, String dataId, String profile, long basedVersion, long editVersion,
                       String operator, ConfigOperationEvent operationType, String remarks, String ip, Timestamp operationTime) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.basedVersion = basedVersion;
        this.editVersion = editVersion;
        this.operator = operator;
        this.operationType = operationType;
        this.remarks = remarks;
        this.ip = ip;
        this.operationTime = operationTime;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ConfigOperationEvent getOperationType() {
        return operationType;
    }

    public void setOperationType(ConfigOperationEvent operationType) {
        this.operationType = operationType;
    }

    public String getOperationTypeText() {
        return operationType.text().replace("文件", "") + "了该文件";
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Timestamp getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Timestamp operationTime) {
        this.operationTime = operationTime;
    }

    @Override
    public String toString() {
        return "ConfigOpLog{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", basedVersion=" + basedVersion +
                ", editVersion=" + editVersion +
                ", operator='" + operator + '\'' +
                ", operationType=" + operationType +
                ", remarks='" + remarks + '\'' +
                ", ip='" + ip + '\'' +
                ", operationTime=" + operationTime +
                '}';
    }
}
