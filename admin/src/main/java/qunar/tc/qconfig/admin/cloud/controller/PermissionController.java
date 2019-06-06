package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.GroupOpLogDao;
import qunar.tc.qconfig.admin.model.*;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.bean.PermissionInfoBean;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Controller
@RequestMapping("/qconfig/permission")
public class PermissionController extends AbstractControllerHelper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private UserContextService userContext;

    @Resource
    private GroupOpLogDao groupOpLogDao;

    @Resource
    private CandidateDao candidateDao;

    @Resource
    private ConfigService configService;

    private static final List<PermissionType> FILE_PERMISSIN_TYPES = ImmutableList.of(
            PermissionType.VIEW, PermissionType.EDIT, PermissionType.APPROVE, PermissionType.PUBLISH);

    @RequestMapping("/getPermissionList")
    @ResponseBody
    public Object getPermissionList(@RequestParam String group, @RequestParam(required = false) String dataId) {
        checkArgument(!Strings.isNullOrEmpty(group));
        logger.info("get permission list, group=[{}], dataId=[{}]", group, dataId);
        Map<String, Object> resultMap = Maps.newHashMap();
        try {
            Map<String, Permission> permissionInfoMap = getPermissionInfos(group, dataId);
            List<PermissionInfoBean> beans = generatePermissionInfoBeanList(permissionInfoMap);

            List<GroupOpLog> groupOpLogs = groupOpLogDao.selectRecentByGroup(group);
            resultMap.put("permissionList", beans);
            resultMap.put("permissionOpLogs", groupOpLogs);
            Set<String> files = Sets.newHashSet();
            for (Candidate candidate : configService.findCandidatesWithGroupAndEnvironment(group, ImmutableSet.of(Environment.PROD, Environment.RESOURCES))) {
                files.add(candidate.getDataId());
            }
            resultMap.put("files", files);

            if (Strings.isNullOrEmpty(dataId)) {
                resultMap.put("dataId", "");
                resultMap.put("rtxIds", ImmutableList.of());
            } else {
                resultMap.put("dataId", dataId);
                List<FilePermissionInfo> filePermissions = permissionService.getFilePermissionListByGroupAndDataId(group, dataId);
                List<String> rtxIds = Lists.newArrayListWithCapacity(filePermissions.size());
                for (FilePermissionInfo permissionInfo : filePermissions) {
                    if (!userContext.getOwners(group).contains(permissionInfo.getRtxId())) {
                        rtxIds.add(permissionInfo.getRtxId());
                    }
                }
                resultMap.put("rtxIds", rtxIds);
            }

            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("get permission list error, group=[{}], dataId=[{}]", group, dataId, e);
            throw e;
        }
    }


    @RequestMapping("/setPermissionList")
    @ResponseBody
    public Object setPermissionList(@RequestParam("group") String group,
                                    @RequestParam(required = false, defaultValue = "") String dataId,
                                    @RequestBody PermissionInfoBean[] permissionInfoBeans) {
        checkLegalGroup(group);
        if (!Strings.isNullOrEmpty(dataId)) {
            checkArgument(candidateDao.existInEnvironment(group, dataId, Environment.PROD) ||
                    candidateDao.existInEnvironment(group, dataId, Environment.RESOURCES), "在group[%s]的prod/resources环境没有[%s]文件", group, dataId);
        }

        for (PermissionInfoBean infoBean : permissionInfoBeans) {
            checkLegalRtxId(infoBean.getRtxId());
        }

        logger.info("set permissions, group=[{}], dataId=[{}], permission infos={}", group, dataId, Arrays.toString(permissionInfoBeans));
        try {
            if (Strings.isNullOrEmpty(dataId)) {
                Monitor.SET_PERMISSION_STATICS.inc();

                permissionService.setPermissionList(group,
                        Lists.newArrayList(permissionInfoBeans).stream()
                                .filter(notOwnerOf(group))
                                .map(beanToModel(group))
                                .collect(Collectors.toList()));
            } else {
                Monitor.SET_FILE_PERMISSION_STATICS.inc();
                permissionService.setPermissionList(group, dataId,
                        Lists.newArrayList(permissionInfoBeans).stream()
                                .filter(notOwnerOf(group))
                                .map(beanToModel(group, dataId))
                                .collect(Collectors.toList()));
            }
            return JsonV2.successOf(true);
        } catch (RuntimeException e) {
            logger.error("set permissions error, group=[{}], dataId=[{}], permission infos={}", group, dataId,
                    Arrays.toString(permissionInfoBeans), e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    @RequestMapping("/deleteFilePermissionWithRtxId")
    @ResponseBody
    public Object deleteFilePermissionWithRtxId(@RequestParam String group, @RequestParam String dataId, @RequestBody String[] rtxIds) {
        checkLegalGroup(group);
        checkLegalDataId(dataId);
        for (String rtxId : rtxIds) {
            checkLegalRtxId(rtxId);
        }

        logger.info("delete file permission, group=[{}], dataId=[{}], rtxId=[{}]", group, dataId, Arrays.toString(rtxIds));
        Monitor.SET_FILE_PERMISSION_STATICS.inc();
        try {
            for (String rtxId : rtxIds) {
                boolean result = permissionService.deleteFilePermission(group, dataId, rtxId);
                if (!result) {
                    throw new RuntimeException("删除[" + rtxId + "]失败!");
                }
            }
            return JsonV2.successOf(true);
        } catch (RuntimeException e) {
            logger.error("delete file permissions error, group=[{}], dataId=[{}], rtxId=[{}]", group, dataId, Arrays.toString(rtxIds));
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    private List<PermissionInfoBean> generatePermissionInfoBeanList(Map<String, Permission> permissionInfoMap) {
        List<PermissionInfoBean> beans = Lists.newArrayListWithCapacity(permissionInfoMap.size());
        for (Permission permission : permissionInfoMap.values()) {
            beans.add(MODEL_TO_BEAN.apply(permission));
        }
        return beans;
    }

    @RequestMapping("/queryFilePermissions")
    @ResponseBody
    public Object getFilePermission(@RequestParam String group, @RequestParam String profile, @RequestParam String dataId) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        Map<String, Boolean> permissions = new HashMap<>();
        for (PermissionType permissionType : FILE_PERMISSIN_TYPES) {
            permissions.put(permissionType.name(), permissionService.hasFilePermission(group, profile, dataId, permissionType));
        }

        return JsonV2.successOf(permissions);
    }

    private Map<String, Permission> getPermissionInfos(String group, String dataId) {
        Map<String, Permission> permissionInfoMap = Maps.newHashMap();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (Strings.isNullOrEmpty(dataId)) {
            Set<String> developers = userContext.getDevelopers(group);
            for (String developer : developers) {
                permissionInfoMap.put(developer, new PermissionInfo(group, developer, PermissionType.VIEW.mask(), now));
            }

            for (PermissionInfo permissionInfo : permissionService.getPermissionListByGroup(group)) {
                permissionInfoMap.put(permissionInfo.getRtxId(), permissionInfo);
            }
        } else {
            for (FilePermissionInfo permissionInfo : permissionService.getFilePermissionListByGroupAndDataId(group, dataId)) {
                permissionInfoMap.put(permissionInfo.getRtxId(), permissionInfo);
            }
        }
        Set<String> owners = userContext.getOwners(group);
        for (String owner : owners) {
            permissionInfoMap.remove(owner);
        }
        return permissionInfoMap;
    }


    private Predicate<PermissionInfoBean> notOwnerOf(final String group) {
        return new Predicate<PermissionInfoBean>() {
            @Override
            public boolean apply(PermissionInfoBean input) {
                return !userContext.getOwners(group).contains(input.getRtxId());
            }
        };
    }



    private Function<PermissionInfoBean, PermissionInfo> beanToModel(final String group) {
        return new Function<PermissionInfoBean, PermissionInfo>() {
            @Override
            public PermissionInfo apply(PermissionInfoBean input) {
                PermissionInfo permissionInfo = new PermissionInfo();

                permissionInfo.setGroup(group);
                permissionInfo.setRtxId(input.getRtxId());

                int permission = 0;
                permission = input.isApprove() ? PermissionType.setPermission(permission, PermissionType.APPROVE)
                        : permission;
                permission = input.isEdit() ? PermissionType.setPermission(permission, PermissionType.EDIT) : permission;
                permission = input.isPublish() ? PermissionType.setPermission(permission, PermissionType.PUBLISH)
                        : permission;

                PermissionType.of(permission); // check legal
                permissionInfo.setPermission(permission);

                return permissionInfo;
            }
        };
    }

    private Function<PermissionInfoBean, FilePermissionInfo> beanToModel(final String group, final String dataId) {
        return new Function<PermissionInfoBean, FilePermissionInfo>() {
            @Override
            public FilePermissionInfo apply(PermissionInfoBean input) {
                FilePermissionInfo permissionInfo = new FilePermissionInfo();

                permissionInfo.setGroup(group);
                permissionInfo.setRtxId(input.getRtxId());

                int permission = 0;
                permission = input.isApprove() ? PermissionType.setPermission(permission, PermissionType.APPROVE)
                        : permission;
                permission = input.isEdit() ? PermissionType.setPermission(permission, PermissionType.EDIT) : permission;
                permission = input.isPublish() ? PermissionType.setPermission(permission, PermissionType.PUBLISH)
                        : permission;

                PermissionType.of(permission); // check legal
                permissionInfo.setPermission(permission);

                permissionInfo.setDataId(dataId);

                return permissionInfo;
            }
        };
    }

    private Function<Permission, PermissionInfoBean> MODEL_TO_BEAN = new Function<Permission, PermissionInfoBean>() {
        @Override
        public PermissionInfoBean apply(Permission input) {
            PermissionInfoBean bean = new PermissionInfoBean();

            bean.setRtxId(input.getRtxId());

            PermissionType.of(input.getPermission()); // check legal
            bean.setApprove(PermissionType.APPROVE.hasPermission(input.getPermission()));
            bean.setEdit(PermissionType.EDIT.hasPermission(input.getPermission()));
            bean.setPublish(PermissionType.PUBLISH.hasPermission(input.getPermission()));

            return bean;
        }
    };

}
