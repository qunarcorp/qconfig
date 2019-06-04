package qunar.tc.qconfig.admin.event;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 14:46
 */
public class PermissionNotifyBean {

    public PermissionNotifyBean(String group, String operator, List<String> remarks, Timestamp opTime) {
        this.group = group;
        this.operator = operator;
        this.remarks = remarks;
        this.opTime = opTime;
    }

    public final String group;
    public final String operator;
    public final List<String> remarks;
    public final Timestamp opTime;

    @Override
    public String toString() {
        return "PermissionNotifyBean{" +
                "group='" + group + '\'' +
                ", operator='" + operator + '\'' +
                ", remarks=" + remarks +
                ", opTime=" + opTime +
                '}';
    }
}
