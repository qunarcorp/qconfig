package qunar.tc.qconfig.admin.model;

/**
 * Created by pingyang.yang on 2018/10/23
 */
public class FileContentMD5 {

    private String group;

    private String profile;

    private String dataId;

    private int version;

    private String md5;

    public FileContentMD5() {
    }

    public FileContentMD5(String group, String profile, String dataId, int version, String md5) {
        this.group = group;
        this.profile = profile;
        this.dataId = dataId;
        this.version = version;
        this.md5 = md5;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "FileContentMD5{" +
                "group='" + group + '\'' +
                ", profile='" + profile + '\'' +
                ", dataId='" + dataId + '\'' +
                ", version=" + version +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
