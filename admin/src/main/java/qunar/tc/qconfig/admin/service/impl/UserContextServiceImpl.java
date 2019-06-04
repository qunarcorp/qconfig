package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.cloud.service.EnvironmentService;
import qunar.tc.qconfig.admin.dao.PermissionDao;
import qunar.tc.qconfig.admin.model.FilePermissionInfo;
import qunar.tc.qconfig.admin.service.ApplicationInfoService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.security.Account;
import qunar.tc.qconfig.admin.web.security.AdminService;
import qunar.tc.qconfig.common.support.Application;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.servercommon.util.QCloudUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 15:09
 */
// TODO: 2019-05-15 这里未处理
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UserContextServiceImpl implements UserContextService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final Application NOTEXIST_APPLICATION = new Application();

    static {
        NOTEXIST_APPLICATION.setCode("");
        NOTEXIST_APPLICATION.setCreateTime(new Date());
        NOTEXIST_APPLICATION.setCreator("");
        NOTEXIST_APPLICATION.setDeveloper(null);
        NOTEXIST_APPLICATION.setGroupCode("");
        NOTEXIST_APPLICATION.setMailGroup(null);
        NOTEXIST_APPLICATION.setName("");
        NOTEXIST_APPLICATION.setOwner(null);
        NOTEXIST_APPLICATION.setStatus(Application.Status.pass);
    }

    @Resource
    private PermissionDao permissionDao;

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private AdminService adminService;

    @Resource
    private ApplicationInfoService applicationInfoService;

    @Resource
    private ProfileService profileService;

    @Resource
    private EnvironmentService environmentService;

    private static ThreadLocal<Account> account = new ThreadLocal<Account>();

    private ThreadLocal<String> ip = new ThreadLocal<String>();

    private ThreadLocal<Map<String, Optional<Integer>>> permissions = new ThreadLocal<Map<String, Optional<Integer>>>();

    // group -> group info
    private ThreadLocal<Map<String, Application>> groups = new ThreadLocal<Map<String, Application>>();

    private ThreadLocal<Map<String, Application>> accountGroups = new ThreadLocal<Map<String, Application>>();

    // 非本人所属的group
    private ThreadLocal<Map<String, Optional<Application>>> extraGroups = new ThreadLocal<Map<String, Optional<Application>>>();

    private ThreadLocal<Set<String>> accessibleGroups = new ThreadLocal<Set<String>>();

    private ThreadLocal<Table<String, String, Integer>> filePermissions = new ThreadLocal<Table<String, String, Integer>>();

    private ThreadLocal<Map<String, Set<Environment>>> groupEnvironments = new ThreadLocal<>();

    private ThreadLocal<Map<String, Map<String, Set<String>>>> allGroupEnvs = new ThreadLocal<>();

    @Override
    public String getRtxId() {
        Account account = this.account.get();
        return account != null ? account.getUserId() : "";
    }

    @Override
    public Account getAccount() {
        return account.get();
    }

    @Override
    public void setAccount(Account account) {
        this.account.set(account);
    }

    @Override
    public String getIp() {
        return ip.get();
    }

    @Override
    public void setIp(String ip) {
        this.ip.set(ip);
    }

    @Override
    public void clear() {
        account.remove();
        ip.remove();
        permissions.remove();
        groups.remove();
        extraGroups.remove();
        accessibleGroups.remove();
        filePermissions.remove();
        groupEnvironments.remove();
        accountGroups.remove();
        allGroupEnvs.remove();

    }

    @Override
    public boolean hasGroupPermission(String group) {
        return isAdmin() || getAccountGroups().contains(group);
    }

    @Override
    public Set<String> getGroups() {
        return groups.get().keySet();
    }

    //格式account:group, 若为特殊account则还是原group形式
    @Override
    public Set<String> getAccountGroups() {
        if (this.accountGroups.get() != null) {
            return accountGroups.get().keySet();
        }
        Set<Map.Entry<String, Application>> groupSet = this.groups.get().entrySet();
        Map<String, Application> accountGroupsMap = Maps.newHashMap();
        for (Map.Entry<String, Application> group : groupSet) {
//            accountGroupsMap.put(getAccount().getCorp() + ":" + group.getKey(), group.getValue());
            accountGroupsMap.put(group.getKey(), group.getValue());
        }
        this.accountGroups.set(accountGroupsMap);
        return accountGroupsMap.keySet();
    }

    @Override
    public Application getApplication(String group) {
        if (groups.get() == null || groups.get().get(group) == null) {
            Application application = getExtraGroupInfo(group);
            return application != null ? application : NOTEXIST_APPLICATION;
        } else {
            return groups.get().get(group);
        }
    }

    @Override
    public Set<String> getEnvs(String group) {
        Map<String, Set<String>> totalEnvs = getGroupTotalEnvs().get(group);
        return totalEnvs != null ? totalEnvs.keySet() : ImmutableSet.<String>of();
    }

    @Override
    public Map<String, Set<String>> getTotalEnvs(String group) {
        String app = QCloudUtils.getAppFromGroup(group);
        Map<String, Set<String>> result = getGroupTotalEnvs().get(app);
        return result != null ? result : ImmutableMap.<String, Set<String>>of();
    }

    @Override
    public Set<Environment> getEnvironments(String group) {
        Map<String, Set<Environment>> map = groupEnvironments.get();
        if (map == null) {
            map = Maps.newHashMap();
            groupEnvironments.set(map);
        }

        Set<Environment> environments = map.get(group);
        if (environments != null) {
            return environments;
        } else {
            environments = Sets.newHashSet();
            map.put(group, environments);
        }

        Map<String, Set<String>> totalEnvs = getTotalEnvs(group);
        for (Map.Entry<String, Set<String>> entry : totalEnvs.entrySet()) {
            String env = entry.getKey();
            environments.add(Environment.generate(env, ""));
            for (String subEnv : entry.getValue()) {
                environments.add(Environment.generate(env, subEnv));
            }
        }
        return environments;
    }

    @Override
    public Set<String> getProfiles(String group) {
        Set<Environment> environments = getEnvironments(group);
        Set<String> profiles = Sets.newHashSetWithExpectedSize(environments.size());
        for (Environment env : environments) {
            profiles.add(env.profile());
        }
        return profiles;
    }

    // todo: qcloud
    @Override
    // group -> env -> subEnv
    //appcode -> env -> group
    public Map<String, Map<String, Set<String>>> getGroupTotalEnvs() {
        if (allGroupEnvs.get() != null) {
            return allGroupEnvs.get();
        }

        Set<String> accessibleGroups = getAccessibleGroups();
        Map<String, Map<String, Set<String>>> envs = getEnvs(accessibleGroups);
        this.allGroupEnvs.set(envs);

        return envs;
    }

    // env -> subenvs
    public Map<String, Set<String>> getGroupEnvs(String group) {
        Map<String, Map<String, Set<String>>> envs = getEnvs(Sets.newHashSet(group));
        return envs.get(group);
    }

    private Map<String, Map<String, Set<String>>> getEnvs(Set<String> groups) {
        Map<String, Map<String, Set<String>>> totalGroupEnvs = Maps.newHashMapWithExpectedSize(groups.size());
        for (String group : groups) {
            totalGroupEnvs.put(group, ImmutableMap.of());
        }

//        this.allGroupEnvs.set(totalGroupEnvs);
        //tole add to profile db
        List<Map.Entry<String, String>> dblist = profileService.find(groups);

        Map<String, Set<String>> profilesInAppcenter = transProfile(totalGroupEnvs);
        //
        Map<String, Set<String>> profilesInDatabase = omitAccount(transProfile(dblist));

        return mergeAndStoreProfiles(profilesInAppcenter, profilesInDatabase);
    }


    //过滤掉group中的account字段
    private Map<String, Set<String>> omitAccount(Map<String, Set<String>> profileMapWithAccount) {
        if (CollectionUtils.isEmpty(profileMapWithAccount)) {
            return ImmutableMap.of();
        }
        Map<String, Set<String>> resultMap = Maps.newHashMapWithExpectedSize(profileMapWithAccount.size());
        for (Map.Entry<String, Set<String>> profileEntry : profileMapWithAccount.entrySet()) {
            String groupWithoutAccount = QCloudUtils.getAppFromGroup(profileEntry.getKey());
            resultMap.put(groupWithoutAccount, profileEntry.getValue());
        }
        return resultMap;
    }

    private Map<String, Set<String>> transProfile(Map<String, Map<String, Set<String>>> allGroupProfiles) {
        if (CollectionUtils.isEmpty(allGroupProfiles)) {
            return ImmutableMap.of();
        }
        Map<String, Set<String>> resultMap = Maps.newHashMapWithExpectedSize(allGroupProfiles.size());
        for (Map.Entry<String, Map<String, Set<String>>> groupProfileEntry : allGroupProfiles.entrySet()) {
            String group = groupProfileEntry.getKey();
            Map<String, Set<String>> envMap = groupProfileEntry.getValue();
            Set<String> profileSet = Sets.newHashSet();
            for (Map.Entry<String, Set<String>> envEntry : envMap.entrySet()) {
                String env = envEntry.getKey();
                Set<String> buildGroups = envEntry.getValue();
                for (String buildGroup : buildGroups) {
                    String profile = env + ":" + buildGroup;
                    profileSet.add(profile);
                }
                // buildGroup为空的情况
                profileSet.add(env + ":");
            }
            resultMap.put(group, profileSet);
        }
        return resultMap;
    }

    private Map<String, Set<String>> transProfile(List<Map.Entry<String, String>> groupProfilesEntryList) {
        if (groupProfilesEntryList == null) {
            return ImmutableMap.of();
        }
        Map<String, Set<String>> resultMap = Maps.newHashMap();

        for (Map.Entry<String, String> groupProfileEntry : groupProfilesEntryList) {
            String group = groupProfileEntry.getKey();
            String profile = groupProfileEntry.getValue();
            if (resultMap.containsKey(group)) {
                resultMap.get(group).add(profile);
            } else {
                Set<String> profileSet = Sets.newHashSet(profile);
                resultMap.put(group, profileSet);
            }
        }
        return resultMap;
    }

    private Map<String, Map<String, Set<String>>> transProfileToEnvBuildGroup(Map<String, Set<String>> profileMap) {
        if (profileMap == null) {
            return ImmutableMap.of();
        }
        Map<String, Map<String, Set<String>>> resultMap = Maps.newHashMapWithExpectedSize(profileMap.size());
        for (Map.Entry<String, Set<String>> profileEntry : profileMap.entrySet()) {
            String group = profileEntry.getKey();
            Set<String> profiles = profileEntry.getValue();
            Map<String, Set<String>> envBuildGroupMap = Maps.newHashMap();
            for (String profile : profiles) {
                String env = ProfileUtil.getEnvironment(profile);
                String buildGroup = ProfileUtil.getBuildGroup(profile);
                if (Strings.isNullOrEmpty(env)) {
                    continue;
                }
                if (envBuildGroupMap.containsKey(env)) {
                    if (!Strings.isNullOrEmpty(buildGroup)) {
                        envBuildGroupMap.get(env).add(buildGroup);
                    }
                } else {
                    Set<String> buildGroupSet = Sets.newHashSet();
                    if (!Strings.isNullOrEmpty(buildGroup)) {
                        buildGroupSet.add(buildGroup);
                    }
                    envBuildGroupMap.put(env, buildGroupSet);
                }
            }
            resultMap.put(group, envBuildGroupMap);
        }
        return resultMap;
    }


    private Map<String, Map<String, Set<String>>> mergeAndStoreProfiles(Map<String, Set<String>> profilesFromAppcenter, Map<String, Set<String>> profilesFromDatabase) {
        //tole not finished yet
//        Set<String> accessibleGroups = getAccessibleGroups();
//        List<Map.Entry<String, String>> dbProfileList = profileService.find(accessibleGroups);
//        Map<String, Set<String>> groupProfilesAppcenter = Maps.newHashMap();
//        Map<String, Set<String>> groupProfilesDatabase = Maps.newHashMap();
        Map<String, Set<String>> allProfiles = Maps.newHashMapWithExpectedSize(profilesFromDatabase.size());
        for (Map.Entry<String, Set<String>> profilesAppcenter : profilesFromAppcenter.entrySet()) {
            String group = profilesAppcenter.getKey();
            Set<String> profilesAc = nullToEmpty(profilesAppcenter.getValue());
            Set<String> profilesDb = nullToEmpty(profilesFromDatabase.get(group));
            Set<String> diff = Sets.difference(profilesAc, profilesDb);
            Set<String> needCreateProfiles = Sets.difference(diff, getDefauleProfiles());
            if (!needCreateProfiles.isEmpty()) {
                profileService.batchCreate(group, needCreateProfiles);
            }
            Set<String> intersectionSet = Sets.union(profilesAc, profilesDb);
            Set<String> intersectionWithDefaultSet = Sets.union(intersectionSet, getDefauleProfiles());
            allProfiles.put(group, intersectionWithDefaultSet);
        }
        return transProfileToEnvBuildGroup(allProfiles);

    }

    private Set<String> nullToEmpty(Set<String> originSet) {
        return originSet == null ? ImmutableSet.of() : originSet;
    }


    //tole qconfig it
    private Set<String> getDefauleProfiles() {
//        return ImmutableSet.of("resources:", "prod:", "beta:", "dev:");
        List<String> defaultEnvs = environmentService.getSystemDefaultEnvs();
        Set<String> defaultProfiles = Sets.newHashSetWithExpectedSize(defaultEnvs.size());
        for (String env : defaultEnvs) {
            defaultProfiles.add(env + ":");
        }
        return defaultProfiles;
    }

    @Override
    public Set<String> getAccessibleGroups() {
        if (accessibleGroups.get() != null) {
            return accessibleGroups.get();
        }
        Set<String> accessibleGroups = Sets.newHashSet(groups.get().keySet());
        accessibleGroups.addAll(filePermissions.get().rowKeySet());
        this.accessibleGroups.set(accessibleGroups);
        return accessibleGroups;
    }


    //格式account:group
    @Override
    public Set<String> getAccessibleAccountGroups() {
//        String account = getAccount().getCorp();
        Set<String> groups = getAccessibleGroups();
        Set<String> accountGroups = Sets.newHashSetWithExpectedSize(groups.size());
        accountGroups.addAll(groups);
        return accountGroups;
    }

    @Override
    public Optional<Integer> getSpecifiedPermissionOf(String group) {
        Map<String, Optional<Integer>> permissionMap = permissions.get();
        if (permissionMap == null) {
            permissionMap = Maps.newHashMap();
            permissions.set(permissionMap);
        }

        Optional<Integer> permission = permissionMap.get(group);
        if (permission == null) {
            permission = Optional.ofNullable(permissionDao.selectPermission(group, getRtxId()));
            permissionMap.put(group, permission);
        }

        return permission;
    }

    @Override
    public Optional<Integer> getSpecifiedPermissionOf(String group, String dataId) {
        return Optional.ofNullable(filePermissions.get().get(group, dataId));
    }

    @Override
    public boolean isAdmin() {
        return adminService.isAdmin(getRtxId());
    }

    // TODO: 2019-05-15 这里处理QA问题
    @Override
    public boolean isQa() {
        return false;
    }

    @Override
    public boolean isLeaderOf(String group) {
        //tole
        group = QCloudUtils.getAppFromGroup(group);
        if (isAdmin()) {
            return true;
        }

        Application application = groups.get().get(group);
        return application != null && application.getOwner() != null && application.getOwner().contains(getRtxId());
    }

    @Override
    public Set<String> getDevelopers(String group) {
        group = QCloudUtils.getAppFromGroup(group);
        logger.debug("get relative people with group [{}]", group);
        Application application = groups.get().get(group);
        if (application == null) {
            application = getExtraGroupInfo(group);
        }

        if (application != null) {
            Set<String> relatives = Sets.newHashSet();

            if (application.getDeveloper() != null) {
                logger.debug("get group [{}] developers {}", group, application.getDeveloper());
                relatives.addAll(application.getDeveloper());
            }

            if (application.getOwner() != null) {
                logger.debug("get group [{}] owners {}", group, application.getOwner());
                relatives.removeAll(application.getOwner());
            }

            return ImmutableSet.copyOf(relatives);
        }
        return ImmutableSet.of();
    }

    @Override
    public Set<String> getOwners(String group) {
        group = QCloudUtils.getAppFromGroup(group);
        Application application = groups.get().get(group);
        if (application == null) {
            application = getExtraGroupInfo(group);
        }

        if (application != null && application.getOwner() != null) {
            return ImmutableSet.copyOf(application.getOwner());
        }

        return ImmutableSet.of();
    }

    private Application getExtraGroupInfo(String group) {
        if (Strings.isNullOrEmpty(group)) {
            return null;
        }
        group = QCloudUtils.getAppFromGroup(group);
        Map<String, Optional<Application>> extras = extraGroups.get();
        if (extras == null) {
            extras = Maps.newHashMap();
            extraGroups.set(extras);
        }

        Optional<Application> application = extras.get(group);
        if (application == null) {
            application = Optional.ofNullable(applicationInfoService.getGroupInfo(group));
            extras.put(group, application);
        }

        return application.orElse(null);
    }

    @Override
    public Set<String> getRelativeMailAddresses(String group, String operator) {
        Set<String> mailAddresses = new HashSet<>();
        if (Strings.isNullOrEmpty(group)) {
            return mailAddresses;
        }
        if (groups.get() == null) {
            setAccount(new Account(operator));
            freshGroupInfos();
        }
        try {
            // tole upload file时groups未初始化
            Application application = groups.get().get(group);
            if (application == null) {
                application = getExtraGroupInfo(group);
            }
            if (application != null) {
                if (application.getOwner() != null) {
                    mailAddresses.addAll(application.getOwner());
                }

                if (application.getDeveloper() != null) {
                    mailAddresses.addAll(application.getDeveloper());
                }

                if (application.getMailGroup() != null) {
                    mailAddresses.addAll(application.getMailGroup());
                }
            }
            mailAddresses.remove("");
        } catch (Exception e) {
            logger.error("获取邮件列表失败", e);
        }
        return mailAddresses;
    }

    @Override
    public void freshGroupInfos() {
        setFilePermissions();

        List<Application> applications = applicationInfoService.getGroupInfos(getRtxId());
        if (applications == null || applications.isEmpty()) {
            groups.set(ImmutableMap.of());
            return;
        }

        Map<String, Application> map = Maps.newHashMap();
        for (Application application : applications) {
            if (isDeveloper(application) || isOwner(application)) {
                map.put(application.getCode(), application);
            }
        }

        groups.set(ImmutableMap.copyOf(map));
    }

    // todo: corp
    private void setFilePermissions() {
//        List<FilePermissionInfo> filePermissionInfos = permissionDao.selectFilePermissionsByRtxId(getRtxId());
        List<FilePermissionInfo> filePermissionInfos = permissionService.getFilePermissionListByRtxId(getRtxId());
        ImmutableTable.Builder<String, String, Integer> builder = ImmutableTable.builder();
        for (FilePermissionInfo filePermissionInfo : filePermissionInfos) {
            builder.put(filePermissionInfo.getGroup(), filePermissionInfo.getDataId(), filePermissionInfo.getPermission());
        }
        filePermissions.set(builder.build());
    }

    private boolean isOwner(Application application) {
        return application != null && application.getOwner() != null && application.getOwner().contains(getRtxId());
    }

    private boolean isDeveloper(Application application) {
        return application != null && application.getDeveloper() != null && application.getDeveloper().contains(getRtxId());
    }

}
