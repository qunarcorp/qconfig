package qunar.tc.qconfig.admin.cloud.vo;

import java.util.Date;

public class PropertiesEntryDiffVo extends PropertiesEntryVo {

    private long lastVersion;

    private String lastValue;

    private String operator;

    private Date createTime;

    public PropertiesEntryDiffVo() {
    }

    public PropertiesEntryDiffVo(String groupId, String groupName, String profile, String dataId, String key,
                                 String value, long version, long lastVersion, String lastValue, String operator, Date createTime) {
        super(groupId, groupName, profile, dataId, key, value, version);
        this.lastVersion = lastVersion;
        this.lastValue = lastValue;
        this.operator = operator;
        this.createTime = createTime;
    }

    public long getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(long lastVersion) {
        this.lastVersion = lastVersion;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "PropertiesEntryDiffVo{" +
                "lastVersion=" + lastVersion +
                ", lastValue='" + lastValue + '\'' +
                ", operator='" + operator + '\'' +
                ", createTime=" + createTime +
                "} " + super.toString();
    }
}
