package qunar.tc.qconfig.admin.event;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 23:05
 */
public class ProfileCreatedBean {

    public final String group;

    public final String profile;

    public final String operator;

    public final Timestamp operatorTime;

    public ProfileCreatedBean(String group, String profile, String operator, Timestamp operatorTime) {
        this.group = group;
        this.profile = profile;
        this.operator = operator;
        this.operatorTime = operatorTime;
    }


    @Override
    public String toString() {
        return "ProfileCreatedBean{" +
                "group='" + group + '\'' +
                ", profile='" + profile + '\'' +
                ", operator='" + operator + '\'' +
                ", operatorTime=" + operatorTime +
                '}';
    }
}
