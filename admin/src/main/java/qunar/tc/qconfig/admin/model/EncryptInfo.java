package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 18:07
 */
public class EncryptInfo {

    private String group;

    private String dataId;

    private String key;

    private String operator;

    private EncryptKeyStatus status;

    private Timestamp createTime;

    private Timestamp updateTime;

    public EncryptInfo() {
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public EncryptKeyStatus getStatus() {
        return status;
    }

    public void setStatus(EncryptKeyStatus status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "EncryptInfo{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", key='" + key + '\'' +
                ", operator='" + operator + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
