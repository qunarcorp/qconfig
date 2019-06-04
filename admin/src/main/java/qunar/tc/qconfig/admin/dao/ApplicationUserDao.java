package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.AccessRoleType;

import java.util.List;

/**
 * Created by pingyang.yang on 2018/11/26
 */
public interface ApplicationUserDao {

    List<String> getAppCodeByRTX(String rtxId, AccessRoleType type);

    List<String> getAppCodeByRTX(String rtxId);

    AccessRoleType getRoleByIDAndAppCode(String rtxId, String appCode);

    List<String> getUserByAppCodeAndRole(String appCode, AccessRoleType type);

    int addAccess(String rtxId, String appCode, AccessRoleType type);

    void batchAdd(List<String> rtxId, String addCode, AccessRoleType type);

    int removeAccess(String rtxId, String appCode);
}
