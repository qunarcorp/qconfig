package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * Created by chenjk on 2018/1/12.
 */
public class ApiGroupIdRel {

    private Long id;

    private String groupId;

    private String targetGroupId;

    private String token;

    private Timestamp dataChangeLasttime;

    public ApiGroupIdRel() {

    }

    public ApiGroupIdRel(String groupId, String targetGroupId) {
        this.groupId = groupId;
        this.targetGroupId = targetGroupId;
    }

    public ApiGroupIdRel(String groupId, String targetGroupId, String token) {
        this.groupId = groupId;
        this.targetGroupId = targetGroupId;
        this.token = token;
    }

    public ApiGroupIdRel(Long id, String groupId, String targetGroupId, Timestamp dataChangeLasttime) {
        this.id = id;
        this.groupId = groupId;
        this.targetGroupId = targetGroupId;
        this.dataChangeLasttime = dataChangeLasttime;
    }

    public ApiGroupIdRel(Long id, String groupId, String targetGroupId, String token, Timestamp dataChangeLasttime) {
        this.id = id;
        this.groupId = groupId;
        this.targetGroupId = targetGroupId;
        this.token = token;
        this.dataChangeLasttime = dataChangeLasttime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(String targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public Timestamp getDataChangeLasttime() {
        return dataChangeLasttime;
    }

    public void setDataChangeLasttime(Timestamp dataChangeLasttime) {
        this.dataChangeLasttime = dataChangeLasttime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
