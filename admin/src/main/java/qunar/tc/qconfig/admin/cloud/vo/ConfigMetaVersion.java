package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

public class ConfigMetaVersion {

    private String group;

    private String dataId;

    private String profile;

    private long version;

    public ConfigMetaVersion() {
    }

    public ConfigMetaVersion(String group, String dataId, String profile, long version) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.version = version;
    }

    public ConfigMetaVersion(VersionData<ConfigMeta> versionMeta) {
        this(versionMeta.getData(), versionMeta.getVersion());
    }

    public ConfigMetaVersion(ConfigMeta meta, long version) {
        this.group = meta.getGroup();
        this.dataId = meta.getDataId();
        this.profile = meta.getProfile();
        this.version = version;
    }

    @JsonIgnore
    public ConfigMeta getConfigMeta() {
        return new ConfigMeta(group, dataId, profile);
    }


    public String getGroup() {
        return group;
    }

    public String getDataId() {
        return dataId;
    }

    public String getProfile() {
        return profile;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ConfigMetaVersion{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", version=" + version +
                '}';
    }
}
