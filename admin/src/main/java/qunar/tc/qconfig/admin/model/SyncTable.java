package qunar.tc.qconfig.admin.model;

public enum SyncTable {
    CONFIG("config_candidate_snapshot", 0),
    REFERENCE("config_reference", 0),
    PUBLIC_STATUS("file_public_status", 1);

    public String tableName;
    public int offsetType;

    SyncTable(String tableName, int offsetType) {
        this.tableName = tableName;
        this.offsetType = offsetType;
    }
}
