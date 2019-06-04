package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.dao.PermissionDao;
import qunar.tc.qconfig.admin.web.security.PermissionType;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2015 2015/2/9 13:20
 */
public interface Permission {

    String getRtxId();

    void setRtxId(String rtxId);

    int getPermission();

    void setPermission(int permission);

    Timestamp getUpdateTime();

    void setUpdateTime(Timestamp time);

    boolean processChange(PermissionDao permissionDao, List<Permission> permissions);

    String generateChangeRemark();

    boolean shouldAddWhenNoRecordBefore(PermissionType permissionType);
}
