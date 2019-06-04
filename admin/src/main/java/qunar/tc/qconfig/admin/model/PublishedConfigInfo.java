package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PublicStatus;

import java.sql.Timestamp;

/**
 * 用于前端config列表页，config配置详细信息
 *
 * Created by chenjk on 2017/8/1.
 */
public class PublishedConfigInfo extends ConfigInfo {

    private long version;

    private PublicStatus publicStatus;

    private boolean hasBeenModified;

    private String groupName;

    public PublishedConfigInfo() {

    }

    public long getVersion() {
        return version;
    }


    // 兼容unpublishedConfigInfo的editVersion字段
    public long getEditVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public PublicStatus getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(PublicStatus publicStatus) {
        this.publicStatus = publicStatus;
    }

    public boolean isHasBeenModified() {
        return hasBeenModified;
    }

    public void setHasBeenModified(boolean hasBeenModified) {
        this.hasBeenModified = hasBeenModified;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }



}
