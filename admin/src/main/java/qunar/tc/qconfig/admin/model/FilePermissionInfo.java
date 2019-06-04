package qunar.tc.qconfig.admin.model;

import com.google.common.collect.Lists;
import qunar.tc.qconfig.admin.dao.PermissionDao;
import qunar.tc.qconfig.admin.web.security.PermissionType;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2015 2015/2/9 10:00
 */
public class FilePermissionInfo implements Permission {

    private String group;

    private String dataId;

    private String rtxId;

    private int permission;

    private Timestamp updateTime;

    public FilePermissionInfo() {
    }

    public FilePermissionInfo(String group, String dataId, String rtxId, int permission, Timestamp updateTime) {
        this.group = group;
        this.dataId = dataId;
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

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
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
        List<FilePermissionInfo> inputs = Lists.newArrayListWithCapacity(permissions.size());
        for (Permission permission : permissions) {
            inputs.add((FilePermissionInfo) permission);
        }
        permissionDao.batchInsertOrUpdateFilePermission(inputs);
        return true;
    }

    @Override
    public String generateChangeRemark() {
        return "设置" + getRtxId() + "对文件" + getDataId() + "的权限为" + PermissionType.of(getPermission()).text();
    }

    @Override
    public boolean shouldAddWhenNoRecordBefore(PermissionType permissionType) {
        return true;
    }

    @Override
    public String toString() {
        return "FilePermissionInfo{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", rtxId='" + rtxId + '\'' +
                ", permission=" + permission +
                ", updateTime=" + updateTime +
                '}';
    }
}
