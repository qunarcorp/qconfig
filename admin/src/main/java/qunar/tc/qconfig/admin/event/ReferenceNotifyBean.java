package qunar.tc.qconfig.admin.event;

import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 13:41
 */
public class ReferenceNotifyBean {

    public final Reference reference;

    public final RefChangeType changeType;

    public ReferenceNotifyBean(Reference reference, RefChangeType changeType) {
        this.reference = reference;
        this.changeType = changeType;
    }

    @Override
    public String toString() {
        return "ReferenceNotifyBean{" +
                "reference=" + reference +
                ", changeType=" + changeType +
                '}';
    }
}
