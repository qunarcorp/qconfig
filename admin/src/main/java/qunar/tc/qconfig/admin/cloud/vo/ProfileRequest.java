package qunar.tc.qconfig.admin.cloud.vo;

public class ProfileRequest {

    private String group;

    private String profile;

    public ProfileRequest() {
    }

    public ProfileRequest(String group, String profile) {
        this.group = group;
        this.profile = profile;
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

    @Override
    public String toString() {
        return "ProfileRequest{" +
                "group='" + group + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
