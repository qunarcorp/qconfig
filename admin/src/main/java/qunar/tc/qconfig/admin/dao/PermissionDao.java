package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.FilePermissionInfo;
import qunar.tc.qconfig.admin.model.PermissionInfo;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 11:55
 */
public interface PermissionDao {

    Integer selectPermission(String group, String rtxId);

    List<PermissionInfo> selectPermissionsByGroup(String group);

    List<PermissionInfo> selectPermissionsByRtxId(String rtxId);

    void batchInsertOrUpdatePermission(List<PermissionInfo> changes);

    List<FilePermissionInfo> selectFilePermissionsByGroupAndDataId(String group, String dataId);

    List<FilePermissionInfo> selectFilePermissionsByRtxId(String rtxId);

    List<FilePermissionInfo> selectFilePermissionsByGroupAndRtxId(String group, String rtxId);

    void batchInsertOrUpdateFilePermission(List<FilePermissionInfo> changes);

    int deleteFilePermission(String group, String dataId, String rtxId);
}
