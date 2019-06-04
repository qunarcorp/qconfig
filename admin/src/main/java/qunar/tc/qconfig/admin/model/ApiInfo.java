package qunar.tc.qconfig.admin.model;

import java.util.List;

/**
 * Created by chenjk on 2018/1/16.
 */
public class ApiInfo {

    private long id;

    private long groupid;

    private long targetGroupid;

    private List<ApiPermission> apiPermissionList;

    public ApiInfo() {

    }

    public ApiInfo(long id, long groupid, long targetGroupid) {
        this.id = id;
        this.groupid = groupid;
        this.targetGroupid = targetGroupid;
    }

    public ApiInfo(long id, long groupid, long targetGroupid, List<ApiPermission> apiPermissionList) {
        this.id = id;
        this.groupid = groupid;
        this.targetGroupid = targetGroupid;
        this.apiPermissionList = apiPermissionList;
    }

    public List<ApiPermission> getApiPermissionList() {
        return apiPermissionList;
    }

    public void setApiPermissionList(List<ApiPermission> apiPermissionList) {
        this.apiPermissionList = apiPermissionList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupid() {
        return groupid;
    }

    public void setGroupid(long groupid) {
        this.groupid = groupid;
    }

    public long getTargetGroupid() {
        return targetGroupid;
    }

    public void setTargetGroupid(long targetGroupid) {
        this.targetGroupid = targetGroupid;
    }
}