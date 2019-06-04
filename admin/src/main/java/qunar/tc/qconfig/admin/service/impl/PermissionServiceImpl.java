package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.PermissionDao;
import qunar.tc.qconfig.admin.model.FilePermissionInfo;
import qunar.tc.qconfig.admin.model.Permission;
import qunar.tc.qconfig.admin.model.PermissionInfo;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.AdminUtil;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.EnvironmentHelper;
import qunar.tc.qconfig.common.util.ProfileUtil;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 15:32
 */
@Service("acturalPermissionService")
public class PermissionServiceImpl implements PermissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private PermissionDao permissionDao;

    @Resource
    private UserContextService userContext;

    @Override
    public List<PermissionInfo> getPermissionListByGroup(String group) {
        List<PermissionInfo> permissionInfos = permissionDao.selectPermissionsByGroup(group);
        removeAccountFromRtxId(permissionInfos);
        return permissionInfos;
    }

    @Override
    public List<FilePermissionInfo> getFilePermissionListByGroupAndDataId(String group, String dataId) {
        List<FilePermissionInfo> filePermissionInfos = permissionDao.selectFilePermissionsByGroupAndDataId(group, dataId);
        removeAccountFromRtxId(filePermissionInfos);
        return filePermissionInfos;
    }


    private <T extends Permission> void removeAccountFromRtxId(List<T> permissions) {
        for (Permission p : permissions) {
            p.setRtxId(p.getRtxId());
        }
    }

    private List<String> setPermissionList(List<Permission> newer, List<Permission> older) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        List<String> remarks = Lists.newArrayList();
        List<Permission> changes = Lists.newArrayList();

        Map<String, Permission> infoMap = mapByRtxId(newer);
        Map<String, Permission> originInfoMap = mapByRtxId(older);

        MapDifference<String, Permission> difference = Maps.difference(infoMap, originInfoMap, new Equivalence<Permission>() {
            @Override
            protected boolean doEquivalent(Permission a, Permission b) {
                return a.getPermission() == b.getPermission() && a.getRtxId().equals(b.getRtxId());
            }

            @Override
            protected int doHash(Permission permissionInfo) {
                return Objects.hashCode(permissionInfo.getRtxId(), permissionInfo.getPermission());
            }
        });

        dealOnlyOnLeft(changes, remarks, infoMap, difference, now);
        dealDifference(changes, remarks, infoMap, originInfoMap, difference, now);

        if (!changes.isEmpty() && changes.get(0).processChange(permissionDao, changes)) {
            logger.info("change permissions successOf, {}", changes);
        }

        return remarks;
    }

    @Override
    @Transactional
    public List<String> setPermissionList(String group, List<PermissionInfo> permissionInfos) {
        addAccountToRtxId(permissionInfos);
        return setPermissionList(AdminUtil.toSuper(permissionInfos),
                AdminUtil.toSuper(permissionDao.selectPermissionsByGroup(group)));
    }

    @Override
    @Transactional
    public List<String> setPermissionList(String group, String dataId, List<FilePermissionInfo> permissionInfos) {
        addAccountToRtxId(permissionInfos);
        return setPermissionList(AdminUtil.toSuper(permissionInfos),
                AdminUtil.toSuper(permissionDao.selectFilePermissionsByGroupAndDataId(group, dataId)));
    }

    private <T extends Permission> void addAccountToRtxId(List<T> permissions) {
        for (Permission p : permissions) {
            p.setRtxId(p.getRtxId());
        }
    }

    private void dealDifference(List<Permission> changes, List<String> remarks, Map<String, Permission> infoMap,
                                Map<String, Permission> originInfoMap,
                                MapDifference<String, Permission> difference, Timestamp now) {
        for (String rtxId : difference.entriesDiffering().keySet()) {
            Permission left = infoMap.get(rtxId);
            Permission right = originInfoMap.get(rtxId);
            if (left.getPermission() != right.getPermission()) {
                left.setUpdateTime(now);
                changes.add(left);
                String remark = left.generateChangeRemark();
                if (!Strings.isNullOrEmpty(remark)) {
                    remarks.add(remark);
                }
            }
        }
    }

    private void dealOnlyOnLeft(List<Permission> changes, List<String> remarks, Map<String, Permission> infoMap,
                                MapDifference<String, Permission> difference, Timestamp now) {
        for (String rtxId : difference.entriesOnlyOnLeft().keySet()) {
            Permission permissionInfo = infoMap.get(rtxId);
            if (permissionInfo.shouldAddWhenNoRecordBefore(PermissionType.of(permissionInfo.getPermission()))) {
                permissionInfo.setUpdateTime(now);
                changes.add(permissionInfo);
                String remark = permissionInfo.generateChangeRemark();
                if (!Strings.isNullOrEmpty(remark)) {
                    remarks.add(remark);
                }
            }
        }
    }

    private Map<String, Permission> mapByRtxId(Iterable<Permission> permissionInfos) {
        Map<String, Permission> map = Maps.newHashMap();
        for (Permission permissionInfo : permissionInfos) {
            map.put(permissionInfo.getRtxId(), permissionInfo);
        }
        return map;
    }

    @Override
    public List<PermissionInfo> getPermissionListByRtxId(String rtxId) {
        return permissionDao.selectPermissionsByRtxId(rtxId);
    }

    @Override
    public List<FilePermissionInfo> getFilePermissionListByRtxId(String rtxId) {
        return permissionDao.selectFilePermissionsByRtxId(rtxId);
    }

    @Override
    public List<FilePermissionInfo> getFilePermissionListByGroupAndRtxId(String group, String rtxId) {
        return permissionDao.selectFilePermissionsByGroupAndRtxId(group, rtxId);
    }

    @Override
    public boolean deleteFilePermission(String group, String dataId, String rtxId) {
        return permissionDao.deleteFilePermission(group, dataId, rtxId) != 0;
    }

    @Override
    public boolean hasFilePermission(String group, String profile, String dataId, PermissionType permissionType) {
        if (Strings.isNullOrEmpty(group)) {
            return false;
        }

        if (userContext.isLeaderOf(group)) {
            return true;
        }

        if (Strings.isNullOrEmpty(profile) || Strings.isNullOrEmpty(dataId)) {
            return false;
        }

        Environment environment = Environment.fromProfile(profile);
        boolean affectProd = ProfileUtil.affectProd(profile);

        // 非group内人员, 在prod/resources环境可以配指定文件权限
        if (!userContext.getAccountGroups().contains(group)) {
            if (!affectProd) return false;
            Optional<Integer> specifiedPermission = userContext.getSpecifiedPermissionOf(group, dataId);
            if (specifiedPermission.isPresent() && permissionType == PermissionType.VIEW) return true;
            return specifiedPermission.isPresent() && (getPermissionNeed(permissionType, environment) & specifiedPermission.get()) != 0;
        }

        boolean groupPermission = hasPermission(group, profile, permissionType);

        // 文件权限指定只对prod/resources环境生效
        if (groupPermission || !affectProd) {
            return groupPermission;
        }

        Optional<Integer> specifiedPermission = userContext.getSpecifiedPermissionOf(group, dataId);

        // 没有指定文件权限
        if (!specifiedPermission.isPresent()) {
            return groupPermission;
        }

        if (permissionType == PermissionType.VIEW) {
            return true;
        }

        return (getPermissionNeed(permissionType, environment) & specifiedPermission.get()) != 0;
    }

    @Override
    public boolean hasPermission(String group, String profile, PermissionType permissionType) {
        if (Strings.isNullOrEmpty(group)) {
            return false;
        }

        if (userContext.isAdmin()) {
            return true;
        }

        if (!userContext.getAccountGroups().contains(group)) {
            return permissionType == PermissionType.VIEW
                    && !getFilePermissionListByGroupAndRtxId(group, userContext.getRtxId()).isEmpty();
        }

        if (userContext.isLeaderOf(group)) {
            return true;
        }

        if (permissionType == PermissionType.LEADER) {
            return false;
        }

        if (permissionType == PermissionType.VIEW) {
            return true;
        }

        Environment environment;
        if (Strings.isNullOrEmpty(profile)) {
            environment = Environment.RESOURCES;
        } else {
            environment = Environment.fromProfile(profile);
        }

        // dev下开发拥有所有权限
        if (environment.isDev()) {
            return true;
        }

        // beta下开发默认拥有编辑权限
        if (environment.isBeta() && permissionType == PermissionType.EDIT) {
            return true;
        }

        // beta下qa默认拥有所有权限
        if (environment.isBeta() && userContext.isQa()) {
            return true;
        }

        return (getPermissionNeed(permissionType, environment) & getPermissionWhenUserHasInGroup(group)) != 0;
    }

    private Integer getPermissionNeed(PermissionType permissionType, Environment environment) {
        //tole 支持旧有环境

        Integer permissionCode = PERMISSION_TABLE.get(environment.getEnvType(), permissionType);
        if (permissionCode == null) {
            return PERMISSION_MAP.get(permissionType);
        }
        return permissionCode;
    }

    private int getPermissionWhenUserHasInGroup(String group) {
        Optional<Integer> permission = userContext.getSpecifiedPermissionOf(group);
        return permission.orElseGet(PermissionType.VIEW::mask);
    }

    private static final ImmutableMap<PermissionType, Integer> PERMISSION_MAP = ImmutableMap.of(
            PermissionType.EDIT, PermissionType.EDIT.mask(),
            PermissionType.APPROVE, PermissionType.APPROVE.mask(),
            PermissionType.PUBLISH, PermissionType.PUBLISH.mask());

    // 环境env下进行PermissionType的操作所需要的权限
    private static final ImmutableTable<EnvironmentHelper.EnvType, PermissionType, Integer> PERMISSION_TABLE = ImmutableTable.<EnvironmentHelper.EnvType, PermissionType, Integer>builder()
            .put(EnvironmentHelper.EnvType.RESOURCES, PermissionType.EDIT, PermissionType.EDIT.mask())
            .put(EnvironmentHelper.EnvType.RESOURCES, PermissionType.APPROVE, PermissionType.APPROVE.mask())
            .put(EnvironmentHelper.EnvType.RESOURCES, PermissionType.PUBLISH, PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.PROD, PermissionType.EDIT, PermissionType.EDIT.mask())
            .put(EnvironmentHelper.EnvType.PROD, PermissionType.APPROVE, PermissionType.APPROVE.mask())
            .put(EnvironmentHelper.EnvType.PROD, PermissionType.PUBLISH, PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.BETA, PermissionType.EDIT, PermissionType.EDIT.mask() | PermissionType.APPROVE.mask() | PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.BETA, PermissionType.APPROVE, PermissionType.APPROVE.mask() | PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.BETA, PermissionType.PUBLISH, PermissionType.APPROVE.mask() | PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.DEV, PermissionType.EDIT, PermissionType.EDIT.mask() | PermissionType.APPROVE.mask() | PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.DEV, PermissionType.APPROVE, PermissionType.EDIT.mask() | PermissionType.APPROVE.mask() | PermissionType.PUBLISH.mask())
            .put(EnvironmentHelper.EnvType.DEV, PermissionType.PUBLISH, PermissionType.EDIT.mask() | PermissionType.APPROVE.mask() | PermissionType.PUBLISH.mask())
            .build();

}
