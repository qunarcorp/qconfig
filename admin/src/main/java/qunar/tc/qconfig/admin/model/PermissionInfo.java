package qunar.tc.qconfig.admin.model;

import com.google.common.collect.Lists;
import qunar.tc.qconfig.admin.dao.PermissionDao;
import qunar.tc.qconfig.admin.web.security.PermissionType;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 15:13
 */
public class PermissionInfo implements Permission {

    private String group;

    private String rtxId;

    private int permission;

    private Timestamp updateTime;

    public PermissionInfo() {
    }

    public PermissionInfo(String group, String rtxId, int permission, Timestamp updateTime) {
        this.group = group;
        this.rtxId = rtxId;
        this.permission = permission;
        this.updateTime = updateTime;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRtxId() {
        return rtxId;
    }

    public void setRtxId(String rtxId) {
        this.rtxId = rtxId;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean processChange(PermissionDao permissionDao, List<Permission> permissions) {
        List<PermissionInfo> inputs = Lists.newArrayListWithCapacity(permissions.size());
        for (Permission permission : permissions) {
            inputs.add((PermissionInfo) permission);
        }
        permissionDao.batchInsertOrUpdatePermission(inputs);
        return true;
    }

    @Override
    public String generateChangeRemark() {
        return "设置" + getRtxId() + "权限为" + PermissionType.of(getPermission()).text();
    }

    @Override
    public boolean shouldAddWhenNoRecordBefore(PermissionType permissionType) {
        return permissionType != PermissionType.VIEW;
    }

    @Override
    public String toString() {
        return "PermissionInfo{" +
                "group='" + group + '\'' +
                ", rtxId='" + rtxId + '\'' +
                ", permission=" + permission +
                ", updateTime=" + updateTime +
                '}';
    }
}
