package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

public class SyncRecordOffset {

    private long lastId;

    private Timestamp lastUpdateTime;

    public SyncRecordOffset() {
    }

    public SyncRecordOffset(long lastId, Timestamp lastUpdateTime) {
        this.lastId = lastId;
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "SyncOffset{" +
                "lastId=" + lastId +
                ", lastUpdateTime=" + lastUpdateTime +
                '}';
    }
}
