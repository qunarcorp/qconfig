package qunar.tc.qconfig.admin.dto;

/**
 * @author pingyang.yang
 * @since 2018/9/28
 */
public class TemplateVersionDTO {

    private String group;

    private String template;

    private int currentVersion;

    private int targetVersion;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public int getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(int targetVersion) {
        this.targetVersion = targetVersion;
    }
}
