package qunar.tc.qconfig.admin.dto;

/**
 * @author pingyang.yang
 * @since 2018/9/28
 */
public class TemplateUpdateDTO {

    private String group;

    private String dataId;

    private String profile;

    private String template;

    private String templateGroup;

    private int templateVersion;

    private long version;

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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplateGroup() {
        return templateGroup;
    }

    public void setTemplateGroup(String templateGroup) {
        this.templateGroup = templateGroup;
    }

    public int getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(int templateVersion) {
        this.templateVersion = templateVersion;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
