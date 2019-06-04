package qunar.tc.qconfig.servercommon.bean;

import qunar.tc.qconfig.common.util.PublicType;

/**
 * public文件信息
 *
 * Created by chenjk on 2017/8/4.
 */
public class PublicConfigInfo {

    private ConfigMetaWithoutProfile configMetaWithoutProfile;

    private PublicType publicType;

    public PublicConfigInfo() {

    }

    public PublicConfigInfo(String groupId, String dataId) {
        this.configMetaWithoutProfile = new ConfigMetaWithoutProfile(groupId, dataId);
    }

    public PublicConfigInfo(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        this.configMetaWithoutProfile = configMetaWithoutProfile;
    }

    public PublicConfigInfo(ConfigMetaWithoutProfile configMetaWithoutProfile, PublicType publicType) {
        this.configMetaWithoutProfile = configMetaWithoutProfile;
        this.publicType = publicType;
    }

    public ConfigMetaWithoutProfile getConfigMetaWithoutProfile() {
        return configMetaWithoutProfile;
    }

    public void setConfigMetaWithoutProfile(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        this.configMetaWithoutProfile = configMetaWithoutProfile;
    }

    public PublicType getPublicType() {
        return publicType;
    }

    public void setPublicType(PublicType publicType) {
        this.publicType = publicType;
    }
}
