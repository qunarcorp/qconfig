package qunar.tc.qconfig.server.bean;

import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * Created by chenjk on 2018/7/11.
 */
public class LogEntry {
    private Log log;
    private ConfigMeta sourceMeta;
    private ConfigMeta realMeta;
    private Long basedVersion;
    private ConfigUsedType configUsedType;
    private ConfigLogType configLogType;
    private String remarks;

    public LogEntry() {
    }

    public LogEntry(Log log, ConfigMeta sourceMeta, ConfigMeta realMeta, Long basedVersion) {
        this.log = log;
        this.sourceMeta = sourceMeta;
        this.realMeta = realMeta;
        this.basedVersion = basedVersion;
    }

    public LogEntry(Log log, ConfigMeta sourceMeta, ConfigMeta realMeta, Long basedVersion, ConfigUsedType configUsedType, ConfigLogType configLogType, String remarks) {
        this.log = log;
        this.sourceMeta = sourceMeta;
        this.realMeta = realMeta;
        this.basedVersion = basedVersion;
        this.configUsedType = configUsedType;
        this.configLogType = configLogType;
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ConfigUsedType getConfigUsedType() {
        return configUsedType;
    }

    public void setConfigUsedType(ConfigUsedType configUsedType) {
        this.configUsedType = configUsedType;
    }

    public ConfigLogType getConfigLogType() {
        return configLogType;
    }

    public void setConfigLogType(ConfigLogType configLogType) {
        this.configLogType = configLogType;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public ConfigMeta getSourceMeta() {
        return sourceMeta;
    }

    public void setSourceMeta(ConfigMeta sourceMeta) {
        this.sourceMeta = sourceMeta;
    }

    public ConfigMeta getRealMeta() {
        return realMeta;
    }

    public void setRealMeta(ConfigMeta realMeta) {
        this.realMeta = realMeta;
    }

    public Long getBasedVersion() {
        return basedVersion;
    }

    public void setBasedVersion(Long basedVersion) {
        this.basedVersion = basedVersion;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "log=" + log +
                ", sourceMeta=" + sourceMeta +
                ", realMeta=" + realMeta +
                ", basedVersion=" + basedVersion +
                ", configUsedType=" + configUsedType +
                ", configLogType=" + configLogType +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
