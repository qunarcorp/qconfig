package qunar.tc.qconfig.admin.web.bean;

import java.util.Objects;

/**
 * 配置文件详细信息
 * <p>
 * Created by chenjk on 2017/8/17.
 */
public class ConfigDetail {

    private ConfigField configField;

    private long version;

    private String content;

    private String checksum;

    public ConfigDetail(String groupId, String dataId, String profile, long version, String content) {
        this(groupId, dataId, profile, version, content, null);
    }

    public ConfigDetail(String groupId, String dataId, String profile, long version, String content, String checksum) {
        this(new ConfigField(groupId, dataId, profile), version, content, checksum);
    }


    public ConfigDetail(ConfigField configField, long version, String content, String checksum) {
        this.configField = configField;
        this.version = version;
        this.content = content;
        this.checksum = checksum;
    }

    public ConfigField getConfigField() {
        return configField;
    }

    public void setConfigField(ConfigField configField) {
        this.configField = configField;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigDetail that = (ConfigDetail) o;
        return version == that.version &&
                Objects.equals(configField, that.configField) &&
                Objects.equals(content, that.content) &&
                Objects.equals(checksum, that.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configField, version, content, checksum);
    }

    @Override
    public String toString() {
        return "ConfigDetail{" +
                configField.toString() + '\'' +
                ", version='" + version + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }

}
