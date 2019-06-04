package qunar.tc.qconfig.admin.model;

/**
 * @author yunfeng.yang
 * @since 2017/4/25
 */
public class QConfigFileType {
    private String fileType;
    private String suffix;
    private String description;
    private String icon;

    public QConfigFileType() {
    }

    public QConfigFileType(String fileType, String suffix, String description, String icon) {
        this.fileType = fileType;
        this.suffix = suffix;
        this.description = description;
        this.icon = icon;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
