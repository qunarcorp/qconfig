package qunar.tc.qconfig.admin.dto;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/20 11:31
 */
public class PushDto {

    private String group;

    private String dataId;

    private String profile;

    private long basedVersion = 0;

    private long editVersion = 0;

    List<PushItemDto> pushItems;

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

    public List<PushItemDto> getPushItems() {
        return pushItems;
    }

    public void setPushItems(List<PushItemDto> pushItems) {
        this.pushItems = pushItems;
    }

    @Override
    public String toString() {
        return "PushDto{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", basedVersion=" + basedVersion +
                ", editVersion=" + editVersion +
                ", pushItems=" + pushItems +
                '}';
    }
}
