package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.web.security.Account;
import qunar.tc.qconfig.common.support.Application;
import qunar.tc.qconfig.common.util.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 15:06
 */
public interface UserContextService {

    String getRtxId();

    Account getAccount();

    void setAccount(Account account);

    String getIp();

    void setIp(String ip);

    void clear();

    boolean hasGroupPermission(String group);

    Set<String> getGroups();

    Set<String> getAccountGroups();

    Set<String> getEnvs(String group);

    Map<String, Set<String>> getTotalEnvs(String group);

    Set<Environment> getEnvironments(String group);

    Set<String> getProfiles(String group);

    Map<String, Map<String, Set<String>>> getGroupTotalEnvs();

    // 获取任意指定的group的环境信息，无需权限
    Map<String, Set<String>> getGroupEnvs(String group);

    Set<String> getAccessibleGroups();

    Set<String> getAccessibleAccountGroups();

    Optional<Integer> getSpecifiedPermissionOf(String group);

    Optional<Integer> getSpecifiedPermissionOf(String group, String dataId);

    boolean isAdmin();

    boolean isLeaderOf(String group);

    Set<String> getOwners(String group);

    Set<String> getRelativeMailAddresses(String group, String opetator);

    Set<String> getDevelopers(String group);

    void freshGroupInfos();

    Application getApplication(String group);

//    String getAccountUser();
}
