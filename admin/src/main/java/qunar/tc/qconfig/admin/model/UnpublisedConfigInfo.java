package qunar.tc.qconfig.admin.model;

/**
 * 未发布配置文件的配置信息
 *
 * Created by chenjk on 2017/8/1.
 */
public class UnpublisedConfigInfo extends ConfigInfo {

    private long basedVersion;

    private long editVersion;

    private boolean published;//该版本是否已经发布

    public UnpublisedConfigInfo() {

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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

}
