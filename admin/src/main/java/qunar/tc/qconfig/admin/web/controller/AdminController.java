package qunar.tc.qconfig.admin.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.controller.FileController;
import qunar.tc.qconfig.admin.cloud.controller.ReferenceFileController;
import qunar.tc.qconfig.admin.dao.ClientLogDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dto.CopyToDTO;
import qunar.tc.qconfig.admin.model.ConfigInfo;
import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.ProfileInfo;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.service.AdminOperateService;
import qunar.tc.qconfig.admin.service.ApplyQueueService;
import qunar.tc.qconfig.admin.service.CheckEnvConflictService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.security.ApplicationManager;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.support.Application;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhenyu.nie created on 2016 2016/5/31 22:02
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends AbstractControllerHelper {

    protected static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private AtomicBoolean deleteStatus = new AtomicBoolean(false);

    private Long deletedCount = 0l;

    @Autowired
    private ClientLogDao clientLogDao;

    @Resource
    private FileController fileController;

    @Resource
    private FileController configController;

    @Resource
    private ReferenceFileController referenceFileController;

    @Resource
    private ApplyQueueService applyQueueService;

    @Resource
    private CheckEnvConflictService checkEnvConflictService;

    @Resource
    private ProfileService profileService;

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private ListeningClientsService listeningClientsService;

    @Resource
    private ApplicationManager applicationManager;

    @Resource
    private UserContextService userContext;

    @Resource
    private AdminOperateService adminOperateService;


    @RequestMapping("/home")
    public String index() {
        return "admin/index";
    }

    @RequestMapping("/deleteFile")
    public String deleteFile() {
        return "admin/deleteFile";
    }

    @RequestMapping("/deleteBuildGroup")
    public String deleteBuildGroup() {
        return "admin/deleteBuildGroup";
    }

    @RequestMapping("/deleteReference")
    public String deleteReference() {
        return "admin/deleteReference";
    }

    @RequestMapping("/deletePublic")
    public String deletePublic() {
        return "admin/deletePublic";
    }

    @RequestMapping(value = "/deleteServer", method = RequestMethod.GET)
    @ResponseBody
    public Object deleteServer(@RequestParam String ip, @RequestParam String env) {
        int result = 0;
        if (!Strings.isNullOrEmpty(ip) && !Strings.isNullOrEmpty(env)) {
            result = adminOperateService.deleteServer(ip);
        }
        return JsonV2.successOf(result);
    }

    @RequestMapping("/deleteclientlogs")
    @ResponseBody
    public Object deletecClientLogs(@RequestParam String endTime) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(endTime), "结束时间不能为空！");
        logger.info("delete profile endTime: " + endTime);

        if (deleteStatus.compareAndSet(false, true)) {
            try {
                for (DbEnv env : DbEnv.values()) {
                    while (true) {
                        List<Long> ids = clientLogDao.selectIds(env, endTime, 1000);
                        clientLogDao.delete(env, ids);
                        deletedCount = deletedCount + ids.size();
                        if (ids.size() != 1000) {
                            break;
                        }
                    }
                }
                return new JsonV2<>(0, null, "删除个数：" + deletedCount);
            } catch (Exception e) {
                logger.error("删除失败", e);
                return new JsonV2<>(1, e.getMessage(), null);
            } finally {
                deleteStatus.compareAndSet(true, false);
                deletedCount = 0l;
            }
        } else {
            return new JsonV2<>(0, null, "删除中: " + deletedCount);
        }
    }

    @RequestMapping("/referenceApp")
    @ResponseBody
    public Object referenceApp(@RequestParam String fromApp, @RequestParam String toApp) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fromApp), "from app is empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toApp), "to app is empty");
        Preconditions.checkArgument(!fromApp.equals(toApp), "from app equals to app");
        logger.info("copy app [{}] to [{}]", fromApp, toApp);

        try {
            Application app = applicationManager.getAppByCode(fromApp);
        } catch (Exception e) {
            return new JsonV2<>(1, "应用" + fromApp + "不存在", null);
        }

        try {
            Application app = applicationManager.getAppByCode(toApp);
        } catch (Exception e) {
            return new JsonV2<>(1, "应用" + toApp + "不存在", null);
        }

        try {
            adminOperateService.referenceGroup(fromApp, toApp);
            return true;
        } catch (Exception e) {
            logger.error("copy app error, [{}] to [{}]", fromApp, toApp, e);
            return new JsonV2<>(-1, e.getClass().getSimpleName() + ":" + e.getMessage(), null);
        }
    }

    private String replaceFatProfileEnv(String srcProfile) {
        if (Strings.isNullOrEmpty(srcProfile)) {
            return "";
        }
        int indexOfColon = srcProfile.indexOf(":");
        Preconditions.checkArgument(indexOfColon > 0, "illegal profile [%s]", srcProfile);
        String env = srcProfile.substring(0, indexOfColon);
        String subEnv;
        if (indexOfColon == srcProfile.length() - 1) {
            subEnv = "";
        } else {
            subEnv = srcProfile.substring(indexOfColon + 1);
        }

        //非fat:% 则不变(如继承自resources:)
        if (!env.toLowerCase().equals("fat")) {
            return srcProfile;
        }

        if ("lpt".equalsIgnoreCase(subEnv)) {
            return "lpt:";
        }
        return "lpt:" + subEnv;
    }


    @RequestMapping("/batchCopyFile")
    @ResponseBody
    public Object batchCopyFile(@RequestBody JsonNode rootNode, @RequestParam String fileType) {
        Preconditions.checkArgument("common".equals(fileType) || "reference".equals(fileType) || "inherit".equals(fileType), "fileType无效");
        JsonNode groupsArrayNode = rootNode.get("groups");
        Preconditions.checkArgument(groupsArrayNode.isArray(), "groups字段必须是array");
        Iterator<JsonNode> it = groupsArrayNode.iterator();
        Map<String, Map<String, String>> allGroupResult = Maps.newHashMap();
        while (it.hasNext()) {
            JsonNode groupNode = it.next();
            String group = groupNode.get("group").asText();
            String srcProfile = groupNode.get("srcProfile").asText();
            String destProfile = groupNode.get("destProfile").asText();
            createSubEnv(group, destProfile);
            Map<String, String> groupResult = batchCopyWithPublish(group, srcProfile, destProfile, fileType);
            allGroupResult.put(group + "/" + srcProfile, groupResult);
        }
        return allGroupResult;
    }

    @RequestMapping("listeningClients")
    @ResponseBody
    public Object getListeningClients(@RequestParam String group,
                                      @RequestParam String profile,
                                      @RequestParam String dataId,
                                      @RequestParam(defaultValue = "10") int timeout) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        try {
            return listeningClientsService.getListeningClients(meta).get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean createSubEnv(String group, String profile) {
        if (!profileService.exist(group, profile)) {
            profileService.create(group, profile);
        }
        return true;
    }

    private Map<String, String> batchCopyWithPublish(String group, String srcProfile, String destProfile, String fileType) {
        ProfileInfo profileInfo = (ProfileInfo) fileController.getProfileInfo(group, srcProfile, null, null);
        Map<String, String> totalResult = Maps.newHashMap();
        if (!profileExist(group, srcProfile)) {
            totalResult.put("error", "profile: [" + srcProfile + "]不存在");
            return totalResult;
        }
        if (!profileExist(group, destProfile)) {
            totalResult.put("error", "profile: [" + destProfile + "]不存在");
            return totalResult;
        }
        for (ConfigInfo configInfo : profileInfo.getPublishedDatas()) {
            ConfigMeta srcMeta = configInfo.getConfigMeta();
            //todo inherit file
            ConfigMeta refInheritConfigMeta = getInheritConfigMeta(configInfo);
            if (refInheritConfigMeta == null && "common".equals(fileType)) {
                Map.Entry<String, String> result = copyFileTo(srcMeta, destProfile, null);
                totalResult.put(result.getKey(), result.getValue());
            }
            if (refInheritConfigMeta != null && "inherit".equals(fileType)) {
                Map.Entry<String, String> result = copyFileTo(srcMeta, destProfile, refInheritConfigMeta);
                totalResult.put(result.getKey(), result.getValue());
            }
        }
        if ("reference".equals(fileType)) {
            for (ConfigInfo configInfo : profileInfo.getReferenceDatas()) {
                ConfigMeta srcMeta = configInfo.getConfigMeta();
                Map.Entry<String, String> result = copyRefFileTo(srcMeta, destProfile, configInfo.getRefConfigMeta());
                totalResult.put(result.getKey(), result.getValue());
            }
        }
        return totalResult;
    }

    private Map.Entry<String, String> copyRefFileTo(ConfigMeta srcMeta, String destProfile, ConfigMeta refMeta) {
        Reference reference = new Reference();
        reference.setGroup(srcMeta.getGroup());
        reference.setAlias(srcMeta.getDataId());
        reference.setProfile(destProfile);
        reference.setRefGroup(refMeta.getGroup());
        reference.setRefProfile(refMeta.getProfile());
        reference.setRefDataId(refMeta.getDataId());
        try {
            referenceFileController.reference(reference);
            return new AbstractMap.SimpleEntry<>(srcMeta.getDataId(), "copy reference OK");
        } catch (Exception e) {
            return new AbstractMap.SimpleEntry<>(srcMeta.getDataId(), "copy reference error: " + e.getMessage());
        }
    }

    private Map.Entry<String, String> copyFileTo(ConfigMeta srcMeta, String destProfile, ConfigMeta refInheritMeta) {
        String sGroup = srcMeta.getGroup();
        String sProfile = srcMeta.getProfile();
        String sDataId = srcMeta.getDataId();
        CopyToDTO copyToDTO = new CopyToDTO();
        copyToDTO.setGroup(sGroup);
        copyToDTO.setSrc(sProfile);
        copyToDTO.setDataId(sDataId);
        copyToDTO.setProfile(destProfile);
        ConfigMeta destMeta = new ConfigMeta(sGroup, sDataId, destProfile);

        StringBuilder stringBuilder = new StringBuilder();

        try {
            if (isFileExists(destMeta)) {
                return new AbstractMap.SimpleEntry<>(sDataId, "该文件已存在");
            }
            stringBuilder.append("copy:").append(configController.copyTo(copyToDTO)).append(", ");
            stringBuilder.append("approve&publish:").append(applyQueueService.approveAndPublish(destMeta)).append(", ");
            // 处理继承文件
            if (refInheritMeta != null) {
                Reference reference = new Reference();
                reference.setType(RefType.INHERIT.value());
                reference.setProfile(destProfile);
                reference.setAlias(sDataId);
                reference.setGroup(sGroup);
                reference.setOperator("system");
                reference.setRefDataId(refInheritMeta.getDataId());
                reference.setRefGroup(refInheritMeta.getGroup());
                String destRefProfile = replaceFatProfileEnv(refInheritMeta.getProfile());
                reference.setRefProfile(destRefProfile);
                referenceDao.create(reference);
                stringBuilder.append("add inherit ref, ");
            }
            stringBuilder.append("OK");
        } catch (Exception e) {
            logger.info("copy error: {}", copyToDTO, e);
            stringBuilder.append("error: ").append(e.getMessage());
        }
        return new AbstractMap.SimpleEntry<>(sDataId, stringBuilder.toString());

    }

    private boolean isFileExists(ConfigMeta meta) {
        Optional<Conflict> conflictOptional = checkEnvConflictService.getConflict(meta);

        if (!conflictOptional.isPresent()) {
            return false;
        }
        Conflict conflict = conflictOptional.get();
        return conflict.getType() != Conflict.Type.EXIST || conflict.getCandidate().getStatus() != StatusType.DELETE;
    }

    private boolean profileExist(String group, String profile) {
        String buildGroup = ProfileUtil.getBuildGroup(profile);
        return Strings.isNullOrEmpty(buildGroup) || profileService.exist(group, profile);
    }

    private ConfigMeta getInheritConfigMeta(ConfigInfo configInfo) {
        if (configInfo.getRefType() != RefType.INHERIT) {
            return null;
        }
        return configInfo.getRefConfigMeta();
    }



}