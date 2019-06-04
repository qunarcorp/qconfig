package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.FilePermissionInfo;
import qunar.tc.qconfig.admin.model.PermissionInfo;
import qunar.tc.qconfig.admin.web.security.PermissionType;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 14:47
 */
public interface PermissionService {

    List<PermissionInfo> getPermissionListByGroup(String group);

    List<FilePermissionInfo> getFilePermissionListByGroupAndDataId(String group, String dataId);

    List<String> setPermissionList(String group, List<PermissionInfo> permissionInfo);

    List<String> setPermissionList(String group, String dataId, List<FilePermissionInfo> permissionInfos);

    List<PermissionInfo> getPermissionListByRtxId(String rtxId);

    List<FilePermissionInfo> getFilePermissionListByRtxId(String rtxId);

    List<FilePermissionInfo> getFilePermissionListByGroupAndRtxId(String group, String rtxId);

    boolean hasFilePermission(String group, String profile, String dataId, PermissionType permissionType);

    boolean hasPermission(String group, String profile, PermissionType permissionType);

    boolean deleteFilePermission(String group, String dataId, String rtxId);

}
