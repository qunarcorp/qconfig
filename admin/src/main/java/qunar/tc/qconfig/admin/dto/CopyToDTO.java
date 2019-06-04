package qunar.tc.qconfig.admin.dto;

/**
 * Created by zhaohui.yu
 * 16/6/21
 */
public class CopyToDTO {

    private String group;

    private String src;

    private String profile;

    private String dataId;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String to) {
        this.profile = to;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
