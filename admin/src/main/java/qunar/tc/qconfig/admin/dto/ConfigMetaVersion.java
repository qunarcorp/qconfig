package qunar.tc.qconfig.admin.dto;

import qunar.tc.qconfig.common.bean.Profileable;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

public class ConfigMetaVersion implements Profileable {
    private ConfigMeta configMeta;
    private long version;

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

    @Override
    public String getProfile() {
        return configMeta.getProfile();
    }

    @Override
    public void setProfile(String profile) {

    }
}
