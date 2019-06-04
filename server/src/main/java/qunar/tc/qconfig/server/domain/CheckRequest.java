package qunar.tc.qconfig.server.domain;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 5:27 PM
 */
public class CheckRequest {
    private String group;

    private String dataId;

    private String profile;

    private String loadProfile;

    private long version;

    public CheckRequest() {
    }

    public CheckRequest(String group, String dataId, String profile, long version) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.version = version;
    }

    public CheckRequest(String group, String dataId, String profile, String loadProfile, long version) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.loadProfile = loadProfile;
        this.version = version;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setLoadProfile(String loadProfile) {
        this.loadProfile = loadProfile;
    }

    public String getGroup() {
        return group;
    }

    public String getDataId() {
        return dataId;
    }

    public String getProfile() {
        return profile;
    }

    public long getVersion() {
        return version;
    }

    public String getLoadProfile() {
        return loadProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckRequest request = (CheckRequest) o;

        if (version != request.version) return false;
        if (group != null ? !group.equals(request.group) : request.group != null) return false;
        if (dataId != null ? !dataId.equals(request.dataId) : request.dataId != null) return false;
        if (profile != null ? !profile.equals(request.profile) : request.profile != null) return false;
        return loadProfile != null ? loadProfile.equals(request.loadProfile) : request.loadProfile == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (dataId != null ? dataId.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (loadProfile != null ? loadProfile.hashCode() : 0);
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "CheckRequest{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", loadProfile='" + loadProfile + '\'' +
                ", version=" + version +
                '}';
    }
}
