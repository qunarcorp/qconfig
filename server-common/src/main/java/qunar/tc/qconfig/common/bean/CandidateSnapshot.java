package qunar.tc.qconfig.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import qunar.tc.qconfig.common.support.Application;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Date;


/**
 * User: zhaohuiyu
 * Date: 5/23/14
 * Time: 3:12 PM
 */
public class CandidateSnapshot implements Profileable {

    private long id;

    private String group;

    private String dataId;

    private String profile;

    private long basedVersion;

    private long editVersion;

    private String data;

    private String operator;

    private StatusType status;

    private Application application;

    private Date updateTime;

    public CandidateSnapshot(Candidate candidate, String data, String operator) {
        this(candidate, candidate.getEditVersion(), data, operator);
    }

    public CandidateSnapshot(String group, String dataId, String profile, String operator, Date updateTime) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.operator = operator;
        this.updateTime = updateTime;
    }

    public CandidateSnapshot(String group, String dataId, String profile, String operator, long editVersion, Date updateTime, StatusType type) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.operator = operator;
        this.updateTime = updateTime;
        this.editVersion = editVersion;
        this.status = type;
    }

    public CandidateSnapshot(Candidate candidate, long editVersion, String data, String operator) {
        this.group = candidate.getGroup();
        this.dataId = candidate.getDataId();
        this.profile = candidate.getProfile();
        this.basedVersion = candidate.getBasedVersion();
        this.editVersion = editVersion;
        this.data = data;
        this.status = candidate.getStatus();
        this.updateTime = candidate.getUpdateTime() != null ? new Date(candidate.getUpdateTime().getTime()) : null;
        this.operator = operator;
    }

    public CandidateSnapshot(String group, String dataId, String profile, long basedVersion,
                             long editVersion, String data, String operator, StatusType status) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.basedVersion = basedVersion;
        this.editVersion = editVersion;
        this.data = data;
        this.operator = operator;
        this.status = status;
    }

    public CandidateSnapshot(long id, String group, String dataId, String profile, long basedVersion,
                             long editVersion, String data, String operator, StatusType status,
                             Date updateTime) {
        this.id = id;
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.basedVersion = basedVersion;
        this.editVersion = editVersion;
        this.data = data;
        this.operator = operator;
        this.status = status;
        this.updateTime = updateTime;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public ConfigMeta getMeta() {
        return new ConfigMeta(group, dataId, profile);
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

    public String getData() {
        return data;
    }

    public String getOperator() {
        return operator;
    }

    public StatusType getStatus() {
        return status;
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

    public void setData(String data) {
        this.data = data;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateSnapshot)) return false;

        CandidateSnapshot cs = (CandidateSnapshot) o;

        if (basedVersion != cs.basedVersion || editVersion != cs.editVersion) return false;
        if (status != null ? !status.equals(cs.status) : cs.status != null) return false;
        if (group != null ? !group.equals(cs.group) : cs.group != null) return false;
        if (dataId != null ? !dataId.equals(cs.dataId) : cs.dataId != null) return false;
        if (profile != null ? !profile.equals(cs.profile) : cs.profile != null) return false;
        if (data != null ? !data.equals(cs.data) : cs.data != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.code() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (dataId != null ? dataId.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (basedVersion >= 0 ? (int) basedVersion : 0);
        result = 31 * result + (editVersion >= 0 ? (int) editVersion : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "CandidateSnapshot{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", basedVersion=" + basedVersion +
                ", editVersion=" + editVersion +
                ", data='" + data + '\'' +
                ", operator='" + operator + '\'' +
                ", status=" + status +
                ", application=" + application +
                ", updateTime=" + updateTime +
                '}';
    }
}
