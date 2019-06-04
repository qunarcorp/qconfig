package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.sql.Timestamp;

public class FixedVersionRecord {

    long id;

    ConfigMeta meta;

    String ip;

    long version;

    String operator;

    Timestamp createTime;

    Timestamp updateTime;

    public FixedVersionRecord() {
    }

    public FixedVersionRecord(long id, ConfigMeta meta, String ip, long version, String operator, Timestamp createTime, Timestamp updateTime) {
        this.id = id;
        this.meta = meta;
        this.ip = ip;
        this.version = version;
        this.operator = operator;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ConfigMeta getMeta() {
        return meta;
    }

    public void setMeta(ConfigMeta meta) {
        this.meta = meta;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
