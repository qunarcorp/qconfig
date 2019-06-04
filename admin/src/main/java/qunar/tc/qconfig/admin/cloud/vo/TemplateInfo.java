package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.model.TemplateType;

/**
 * Created by pingyang.yang on 2018/11/7
 */
public class TemplateInfo {
    private String templateId;

    private String templateGroup;

    private String template;

    private TemplateType templateType;

    private int templateVersion;

    public int getTemplateVersion() {
        return templateVersion;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public void setTemplateVersion(int templateVersion) {
        this.templateVersion = templateVersion;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public void setTemplateGroup(String templateGroup) {
        this.templateGroup = templateGroup;
    }

    public String getTemplateGroup() {
        return this.templateGroup;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return this.template;
    }

}