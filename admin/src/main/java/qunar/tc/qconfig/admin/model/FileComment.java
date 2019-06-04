package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

public class FileComment {

    private ConfigMeta configMeta;
    private long version;
    private String comment;

    public FileComment() {
    }

    public FileComment(ConfigMeta configMeta, long version, String comment) {
        this.configMeta = configMeta;
        this.version = version;
        this.comment = comment;
    }

    public ConfigMeta getConfigMeta() {
        return configMeta;
    }

    public void setConfigMeta(ConfigMeta configMeta) {
        this.configMeta = configMeta;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "FileComment{" +
                "configMeta=" + configMeta +
                ", version=" + version +
                ", comment='" + comment + '\'' +
                '}';
    }
}
