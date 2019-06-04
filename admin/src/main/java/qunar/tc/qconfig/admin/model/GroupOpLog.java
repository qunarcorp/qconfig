package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 20:03
 */
public class GroupOpLog {

    private String group;

    private String operator;

    private String remarks;

    private Timestamp opTime;

    public GroupOpLog() {

    }

    public GroupOpLog(String group, String operator, String remarks, Timestamp opTime) {
        this.group = group;
        this.operator = operator;
        this.remarks = remarks;
        this.opTime = opTime;
    }

    public String getGroup() {
        return group;
    }

    public GroupOpLog setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public GroupOpLog setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public GroupOpLog setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public Timestamp getOpTime() {
        return opTime;
    }

    public GroupOpLog setOpTime(Timestamp opTime) {
        this.opTime = opTime;
        return this;
    }

    @Override
    public String toString() {
        return "PermissionOpLog{" +
                "group='" + group + '\'' +
                ", operator='" + operator + '\'' +
                ", remarks='" + remarks + '\'' +
                ", opTime=" + opTime +
                '}';
    }
}
