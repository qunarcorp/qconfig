package qunar.tc.qconfig.admin.cloud.vo;

public class FileDescriptionVo {
    private String group;
    private String dataId;
    private String description;

    public FileDescriptionVo() {
    }

    public FileDescriptionVo(String group, String dataId, String description) {
        this.group = group;
        this.dataId = dataId;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FileDescriptionVo{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}