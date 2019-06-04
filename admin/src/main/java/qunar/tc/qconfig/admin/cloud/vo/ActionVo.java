package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.dto.CandidateDTO;

public class ActionVo {

    private String group;

    private String dataId;

    private String profile;

    private long editVersion;

    private String comment;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public long getEditVersion() {
        return editVersion;
    }

    public void setEditVersion(long editVersion) {
        this.editVersion = editVersion;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static CandidateDTO toCandidate(ActionVo actionVo) {
        CandidateDTO dto =  new CandidateDTO();
        dto.setGroup(actionVo.getGroup());
        dto.setProfile(actionVo.getProfile());
        dto.setDataId(actionVo.getDataId());
        dto.setEditVersion(actionVo.getEditVersion());
        dto.setComment(actionVo.getComment());
        return dto;
    }

    @Override
    public String toString() {
        return "ActionVo{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", editVersion=" + editVersion +
                ", comment='" + comment + '\'' +
                '}';
    }
}
