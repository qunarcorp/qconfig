package qunar.tc.qconfig.admin.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.admin.web.serializer.JsonStatusDeserializer;
import qunar.tc.qconfig.admin.web.serializer.JsonStatusSerializer;

import static qunar.tc.qconfig.admin.greyrelease.GreyReleaseUtil.EMPTY_UUID;

/**
 * User: zhaohuiyu
 * Date: 5/23/14
 * Time: 3:28 PM
 */
public class CandidateDTO {

    private String uuid = EMPTY_UUID;

    private String group;

    private String dataId;

    private String profile;

    private long basedVersion = 0;

    private long editVersion = 0;

    private StatusType status;

    private String description;

    private String templateGroup;

    private String template;

    private int templateVersion;

    private String data;

    private String jsonDiff;

    private String validateUrl;

    private long defaultConfigId = 0;

    private String inheritGroupId;

    private String inheritDataId;

    private String inheritProfile;

    private String inheritData;

    private String templateDetail;

    private boolean sendMail = false;

    private boolean isForceUpload;

    public RefType refType;

    private String message;

    private String comment;

    public CandidateDTO() {

    }

    public CandidateDTO(String group, String dataId, String profile, String data) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.data = data;
    }

    public CandidateDTO(String group, String dataId, String profile,
                        long basedVersion, long editVersion, StatusType status,
                        String description, String templateGroup, String template,
                        String data, String jsonDiff, String validateUrl, long defaultConfigId,
                        String inheritGroupId, String inheritDataId, String inheritProfile, String inheritData) {

        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.basedVersion = basedVersion;
        this.editVersion = editVersion;
        this.status = status;
        this.description = description;
        this.templateGroup = templateGroup;
        this.template = template;
        this.data = data;
        this.jsonDiff = jsonDiff;
        this.validateUrl = validateUrl;
        this.defaultConfigId = defaultConfigId;
        this.inheritGroupId = inheritGroupId;
        this.inheritDataId = inheritDataId;
        this.inheritProfile = inheritProfile;
        this.inheritData = inheritData;
    }

    public CandidateDTO(String group, String dataId, String profile, long basedVersion, long editVersion, StatusType status, String data) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.basedVersion = basedVersion;
        this.editVersion = editVersion;
        this.status = status;
        this.data = data;
    }

    public CandidateDTO copy() {
        CandidateDTO dto = new CandidateDTO();
        dto.uuid = this.getUuid();
        dto.group = this.getGroup();
        dto.dataId = this.getDataId();
        dto.profile = this.getProfile();
        dto.basedVersion = this.getBasedVersion();
        dto.editVersion = this.getEditVersion();
        dto.status = this.getStatus();
        dto.data = this.getData();
        dto.template = this.getTemplate();
        dto.templateGroup = this.templateGroup;
        dto.templateVersion = this.templateVersion;
        dto.validateUrl = this.validateUrl;
        dto.defaultConfigId = this.defaultConfigId;
        dto.templateDetail = templateDetail;
        dto.inheritDataId = this.inheritDataId;
        dto.inheritGroupId = this.inheritGroupId;
        dto.inheritProfile = this.inheritProfile;
        dto.inheritData = this.inheritData;
        dto.sendMail = this.sendMail;
        dto.refType = this.refType;
        dto.message = this.message;
        dto.comment = this.comment;
        return dto;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public long getBasedVersion() {
        return basedVersion;
    }

    public void setBasedVersion(long basedVersion) {
        this.basedVersion = basedVersion;
    }

    public long getEditVersion() {
        return editVersion;
    }

    public void setEditVersion(long editVersion) {
        this.editVersion = editVersion;
    }

    public String getTemplateGroup() {
        return templateGroup;
    }

    public void setTemplateGroup(String templateGroup) {
        this.templateGroup = templateGroup;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getJsonDiff() {
        return jsonDiff;
    }

    public void setJsonDiff(String jsonDiff) {
        this.jsonDiff = jsonDiff;
    }

    @JsonSerialize(using = JsonStatusSerializer.class)
    public StatusType getStatus() {
        return status;
    }

    @JsonDeserialize(using = JsonStatusDeserializer.class)
    public void setStatus(StatusType status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValidateUrl() {
        return validateUrl;
    }

    public void setValidateUrl(String validateUrl) {
        this.validateUrl = validateUrl;
    }

    public String getInheritGroupId() {
        return inheritGroupId;
    }

    public void setInheritGroupId(String inheritGroupId) {
        this.inheritGroupId = inheritGroupId;
    }

    public String getInheritDataId() {
        return inheritDataId;
    }

    public void setInheritDataId(String inheritDataId) {
        this.inheritDataId = inheritDataId;
    }

    public long getDefaultConfigId() {
        return defaultConfigId;
    }

    public void setDefaultConfigId(long defaultConfigId) {
        this.defaultConfigId = defaultConfigId;
    }

    public String getTemplateDetail() {
        return templateDetail;
    }

    public void setTemplateDetail(String templateDetail) {
        this.templateDetail = templateDetail;
    }

    public String getInheritProfile() {
        return inheritProfile;
    }

    public void setInheritProfile(String inheritProfile) {
        this.inheritProfile = inheritProfile;
    }

    public String getInheritData() {
        return inheritData;
    }

    public void setInheritData(String inheritData) {
        this.inheritData = inheritData;
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
    }

    public boolean isForceUpload() {
        return isForceUpload;
    }

    public void setForceUpload(boolean forceUpload) {
        isForceUpload = forceUpload;
    }

    public int getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(int templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "CandidateDTO{" +
                "uuid='" + uuid + '\'' +
                ", group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", basedVersion=" + basedVersion +
                ", editVersion=" + editVersion +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", templateGroup='" + templateGroup + '\'' +
                ", template='" + template + '\'' +
                ", templateVersion=" + templateVersion +
                ", data='" + data + '\'' +
                ", jsonDiff='" + jsonDiff + '\'' +
                ", validateUrl='" + validateUrl + '\'' +
                ", defaultConfigId=" + defaultConfigId +
                ", inheritGroupId='" + inheritGroupId + '\'' +
                ", inheritDataId='" + inheritDataId + '\'' +
                ", inheritProfile='" + inheritProfile + '\'' +
                ", inheritData='" + inheritData + '\'' +
                ", templateDetail='" + templateDetail + '\'' +
                ", sendMail=" + sendMail +
                ", isForceUpload=" + isForceUpload +
                ", refType=" + refType +
                ", message='" + message + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
