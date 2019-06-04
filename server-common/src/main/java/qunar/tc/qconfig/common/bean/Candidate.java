package qunar.tc.qconfig.common.bean;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 16:42
 */
public class Candidate implements Profileable {

    private static final long INIT_EDIT_VERSION = 1;

    private static final long INIT_BASED_VERSION = 0;

    private String group;

    private String dataId;

    private String profile;

    private long basedVersion;

    private long editVersion;

    private StatusType status;

    private Timestamp updateTime;

    public Candidate(String group, String dataId, String profile) {
        this(group, dataId, profile, INIT_BASED_VERSION, INIT_EDIT_VERSION, StatusType.PENDING);
    }

    public Candidate(String group, String dataId, String profile, long basedVersion) {
        this(group, dataId, profile, basedVersion, INIT_EDIT_VERSION, StatusType.PENDING);
    }

    public Candidate(String group, String dataId, String profile, long basedVersion, long editVersion) {
        this(group, dataId, profile, basedVersion, editVersion, StatusType.PENDING);
    }

    public Candidate(String group, String dataId, String profile, long basedVersion, long editVersion, StatusType status) {
        this(group, dataId, profile, basedVersion, editVersion, status, null);
    }

    public Candidate(String group, String dataId, String profile, long basedVersion, long editVersion, StatusType status, Timestamp updateTime) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.basedVersion = basedVersion;
        this.editVersion = editVersion;
        this.status = status;
        this.updateTime = updateTime;
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

    public long getBasedVersion() {
        return basedVersion;
    }

    public long getEditVersion() {
        return editVersion;
    }

    public StatusType getStatus() {
        return status;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", basedVersion=" + basedVersion +
                ", editVersion=" + editVersion +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate)) return false;

        Candidate candidate = (Candidate) o;

        if (basedVersion != candidate.basedVersion || editVersion != candidate.editVersion) return false;
        if (status != null ? !status.equals(candidate.status) : candidate.status != null) return false;
        if (group != null ? !group.equals(candidate.group) : candidate.group != null) return false;
        if (dataId != null ? !dataId.equals(candidate.dataId) : candidate.dataId != null) return false;
        if (profile != null ? !profile.equals(candidate.profile) : candidate.profile != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.code() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (dataId != null ? dataId.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (basedVersion >= 0 ? (int)basedVersion : 0);
        result = 31 * result + (editVersion >= 0 ? (int)editVersion : 0);

        return result;
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

    public void setBasedVersion(long basedVersion) {
        this.basedVersion = basedVersion;
    }

    public void setEditVersion(long editVersion) {
        this.editVersion = editVersion;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
