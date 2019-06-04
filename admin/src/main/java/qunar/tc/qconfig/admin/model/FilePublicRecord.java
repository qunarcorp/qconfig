package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

public class FilePublicRecord {

    long id;

    String group;

    String dataId;

    Timestamp create_time;

    Timestamp update_time;

    int type;

    public FilePublicRecord() {
    }

    public FilePublicRecord(long id, String group, String dataId, Timestamp create_time, Timestamp update_time, int type) {
        this.id = id;
        this.group = group;
        this.dataId = dataId;
        this.create_time = create_time;
        this.update_time = update_time;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Timestamp getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Timestamp create_time) {
        this.create_time = create_time;
    }

    public Timestamp getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Timestamp update_time) {
        this.update_time = update_time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}