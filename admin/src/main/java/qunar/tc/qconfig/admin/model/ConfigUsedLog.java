package qunar.tc.qconfig.admin.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import qunar.tc.qconfig.admin.support.HostnameUtil;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.IpPort;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 15:43
 */
public class ConfigUsedLog implements IpPort{

    private String group;

    private String dataId;

    private String profile;

    private String sourceGroupId;

    private String sourceDataId;

    private String sourceProfile;

    private String hostname;

    private String ip;

    private int port;

    private int version;

    private ConfigUsedType type;

    private String remarks;

    private Timestamp updateTime;

    private String consumerProfile;

    private boolean fixedVersion;

    public ConfigUsedLog() {
    }

    public ConfigUsedLog(ConfigMeta configMeta, ConfigMeta sourceConfigMeta, ClientData clientData) {
        this.group = configMeta.getGroup();
        this.dataId = configMeta.getDataId();
        this.profile = configMeta.getProfile();
        this.sourceGroupId = sourceConfigMeta.getGroup();
        this.sourceDataId = sourceConfigMeta.getDataId();
        this.sourceProfile = sourceConfigMeta.getProfile();
        this.hostname = clientData.getHostname();
        this.ip = clientData.getIp();
        this.port = clientData.getPort();
    }

    // TODO: 2018/12/11 这里这样是否可以，sourceMeta？
    public ConfigUsedLog(ConfigMeta configMeta, ClientData clientData) {
        this.group = configMeta.getGroup();
        this.dataId = configMeta.getDataId();
        this.profile = configMeta.getProfile();
        this.sourceGroupId = configMeta.getGroup();
        this.sourceDataId = configMeta.getDataId();
        this.sourceProfile = configMeta.getProfile();
        this.hostname = clientData.getHostname();
        this.version = (int) clientData.getVersion();
        this.ip = clientData.getIp();
        this.port = clientData.getPort();
        this.hostname = HostnameUtil.getHostnameFromIp(this.ip);
    }

    public ConfigUsedLog(String group, String dataId, String profile, String sourceGroupId, String sourceDataId,
                         String sourceProfile, String ip, int port, int version, ConfigUsedType type,
                         String consumerProfile, String remarks, Timestamp updateTime) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.sourceGroupId = sourceGroupId;
        this.sourceDataId = sourceDataId;
        this.sourceProfile = sourceProfile;
        this.hostname = HostnameUtil.getHostnameFromIp(ip);
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.type = type;
        this.consumerProfile = consumerProfile;
        this.remarks = remarks;
        this.updateTime = updateTime;
    }

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

    public String getSourceGroupId() {
        return sourceGroupId;
    }

    public void setSourceGroupId(String sourceGroupId) {
        this.sourceGroupId = sourceGroupId;
    }

    public String getSourceDataId() {
        return sourceDataId;
    }

    public void setSourceDataId(String sourceDataId) {
        this.sourceDataId = sourceDataId;
    }

    public String getSourceProfile() {
        return sourceProfile;
    }

    public void setSourceProfile(String sourceProfile) {
        this.sourceProfile = sourceProfile;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ConfigUsedType getType() {
        return type;
    }

    public void setType(ConfigUsedType type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getConsumerProfile() {
        return consumerProfile;
    }

    public void setConsumerProfile(String consumerProfile) {
        this.consumerProfile = consumerProfile;
    }

    public boolean isFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(boolean fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
