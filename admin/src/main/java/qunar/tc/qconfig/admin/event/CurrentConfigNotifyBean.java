package qunar.tc.qconfig.admin.event;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import qunar.tc.qconfig.common.bean.CandidateSnapshot;

/**
 * @author keli.wang
 */
public class CurrentConfigNotifyBean {
    private final ConfigOperationEvent event;
    private final CandidateSnapshot snapshot;

    public CurrentConfigNotifyBean(ConfigOperationEvent event,
                                   CandidateSnapshot snapshot) {
        this.event = event;
        this.snapshot = snapshot;
    }

    public ConfigOperationEvent getEvent() {
        return event;
    }

    public CandidateSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                      .add("event", event)
                      .add("snapshot", snapshot)
                      .toString();
    }
}
