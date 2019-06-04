package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.common.bean.StatusType;

import java.sql.Timestamp;

/**
 * 文件权限抽象类
 *
 * Created by chenjk on 2017/8/1.
 */
public abstract class ConfigInfo {

    private ConfigMeta configMeta;

    private ConfigMeta refConfigMeta;

    private PublicType publicType;

    private RefType refType;

    private StatusType statusType;

    private boolean edit;

    private boolean approve;

    private boolean publish;

    private Timestamp updateTime;

    private String operator;

    private String description;

    private String comment;

    private boolean favoriteFile;

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isApprove() {
        return approve;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public ConfigMeta getConfigMeta() {
        return configMeta;
    }

    public void setConfigMeta(ConfigMeta configMeta) {
        this.configMeta = configMeta;
    }

    public ConfigMeta getRefConfigMeta() {
        return refConfigMeta;
    }

    public void setRefConfigMeta(ConfigMeta refConfigMeta) {
        this.refConfigMeta = refConfigMeta;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public PublicType getPublicType() {
        return publicType;
    }

    public void setPublicType(PublicType publicType) {
        this.publicType = publicType;
    }

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(StatusType statusType) {
        this.statusType = statusType;
    }

    public String getStatusTypeText() {
        StatusType status = getStatusType();
        return status != null ? status.text() : "";
    }

    public boolean isFavoriteFile() {
        return favoriteFile;
    }

    public void setFavoriteFile(boolean favoriteFile) {
        this.favoriteFile = favoriteFile;
    }
}
