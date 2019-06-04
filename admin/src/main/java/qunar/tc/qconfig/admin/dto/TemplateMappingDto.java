package qunar.tc.qconfig.admin.dto;

/**
 * @author pingyang.yang
 * @since 2018/9/25
 */
public class TemplateMappingDto {

    private String groupId;

    private String dataId;

    private int dataVersion;

    private String templateGroup;

    private String template;

    private int templateVersion;

    private String profile;

    public String getGroupId() {
        return groupId;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
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

    public int getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(int templateVersion) {
        this.templateVersion = templateVersion;
    }
}
