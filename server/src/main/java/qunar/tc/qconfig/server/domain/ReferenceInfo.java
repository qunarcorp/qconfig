package qunar.tc.qconfig.server.domain;

import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/7/3 19:05
 */
public class ReferenceInfo {

    private ConfigMeta source;

    private ConfigMeta reference;

    private RefType refType;

    public ReferenceInfo(ConfigMeta source, ConfigMeta reference) {
        this.source = source;
        this.reference = reference;
        this.refType = RefType.REFERENCE;
    }

    public ReferenceInfo(ConfigMeta source, ConfigMeta reference, RefType type) {
        this.source = source;
        this.reference = reference;
        this.refType = type;
    }

    public ConfigMeta getSource() {
        return source;
    }

    public ConfigMeta getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return "ReferenceInfo{" +
               "source=" + source +
               ", reference=" + reference +
               '}';
    }

    public void setSource(ConfigMeta source) {
        this.source = source;
    }

    public void setReference(ConfigMeta reference) {
        this.reference = reference;
    }

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
    }
}
