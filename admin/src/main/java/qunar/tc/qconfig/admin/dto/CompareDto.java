package qunar.tc.qconfig.admin.dto;

/**
 * @author zhenyu.nie created on 2014 2014/11/2 19:59
 */
public class CompareDto {

    private String group;

    private String lProfile;

    private String rProfile;

    public CompareDto() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLProfile() {
        return lProfile;
    }

    public void setLProfile(String lProfile) {
        this.lProfile = lProfile;
    }

    public String getRProfile() {
        return rProfile;
    }

    public void setRProfile(String rProfile) {
        this.rProfile = rProfile;
    }

    @Override
    public String toString() {
        return "CompareDto{" +
                "group='" + group + '\'' +
                ", lProfile='" + lProfile + '\'' +
                ", rProfile='" + rProfile + '\'' +
                '}';
    }
}
