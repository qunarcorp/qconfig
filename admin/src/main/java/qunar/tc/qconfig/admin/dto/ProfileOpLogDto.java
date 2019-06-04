package qunar.tc.qconfig.admin.dto;

import java.sql.Timestamp;

import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.admin.model.ReferenceLog;

/**
 * @author zhenyu.nie created on 2014 2014/11/6 14:38
 */
public class ProfileOpLogDto {

    public enum OpType { CONFIG, REF }

    private OpType opType;

    private ConfigOpLog configOpLog;

    private ReferenceLog referenceLog;

    public ProfileOpLogDto(ConfigOpLog configOpLog) {
        this.opType = OpType.CONFIG;
        this.configOpLog = configOpLog;
    }

    public ProfileOpLogDto(ReferenceLog referenceLog) {
        this.opType = OpType.REF;
        this.referenceLog = referenceLog;
    }

    public OpType getOpType() {
        return opType;
    }

    public ConfigOpLog getConfigOpLog() {
        return configOpLog;
    }

    public ReferenceLog getReferenceLog() {
        return referenceLog;
    }

    public Timestamp getTime() {
        switch (opType) {
            case CONFIG:
                return configOpLog.getOperationTime();
            case REF:
                return referenceLog.getCreateTime();
            default:
                throw new IllegalArgumentException("illegal opType for " + opType);
        }
    }
}
