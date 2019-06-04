package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.CommentVo;
import qunar.tc.qconfig.admin.cloud.vo.FileDescriptionVo;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaRequest;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.dto.CopyToDTO;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.IllegalTemplateException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.exception.StatusMismatchException;
import qunar.tc.qconfig.admin.exception.TemplateChangedException;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.InheritConfigMeta;
import qunar.tc.qconfig.admin.model.VersionDetail;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.EventPostApplyService;
import qunar.tc.qconfig.admin.service.FileCommentService;
import qunar.tc.qconfig.admin.service.FileDescriptionService;
import qunar.tc.qconfig.admin.service.FilePublicStatusService;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.PropertiesCheckService;
import qunar.tc.qconfig.admin.service.QConfigFileTypeService;
import qunar.tc.qconfig.admin.service.ReferenceService;
import qunar.tc.qconfig.admin.service.TableBatchOpWhitelistService;
import qunar.tc.qconfig.admin.service.UserBehaviorService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.impl.InheritConfigServiceImpl;
import qunar.tc.qconfig.admin.service.template.TemplateUtils;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.servercommon.bean.Config;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Controller
@RequestMapping("/qconfig/file")
public class FileController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConfigService configService;

    @Resource
    private FileTemplateService fileTemplateService;

    @Resource
    private EventPostApplyService applyService;

    @Resource
    private FilePublicStatusService filePublicStatusService;

    @Resource(name = "eventPostReferenceService")
    private ReferenceService referenceService;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private FileDescriptionService fileDescriptionService;

    @Resource
    private FileCommentService fileCommentService;

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private UserContextService userContextService;

    @Resource
    UserContextService userContext;

    @Resource
    private QConfigFileTypeService qConfigFileTypeService;

    @Resource
    private ConfigOpLogDao configOpLogDao;

    @Resource
    private InheritConfigServiceImpl inheritConfigService;

    @Resource
    private TableBatchOpWhitelistService tableBatchOpWhitelistService;

    @Resource
    private ConfigEditorSettingsService configEditorSettingsService;

    @Resource
    private PropertiesCheckService propertiesCheckService;

    @Resource
    private UserBehaviorService userBehaviorService;

    @Value("${configLog.showLength}")
    private int configLogLength;


    // 环境的文件列表, 指定dataId时精确匹配文件名并忽略keyword，
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object getProfileInfo(@RequestParam("group") String group, @RequestParam("profile") String profile,
                                 @RequestParam(required = false, defaultValue = "") String dataId,
                                 @RequestParam(required = false, defaultValue = "") String keyword) {
        checkLegalGroup(group);
        checkLegalProfile(profile);

        try {
            return JsonV2.successOf(configService.getProfileInfo(group, profile, dataId, keyword));
        } catch (RuntimeException e) {
            logger.error("get profile info error, group=[{}], profile=[{}], dataId=[{}], keyword=[{}]", group, profile, dataId, keyword, e);
            throw e;
        }
    }


    @RequestMapping(value = "/listPagination", method = RequestMethod.GET)
    @ResponseBody
    public Object getProfileInfo(@RequestParam("group") String group,
                                 @RequestParam("profile") String profile,
                                 @RequestParam(value = "curPage", required = false, defaultValue = "1") int pageNo,
                                 @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
                                 @RequestParam(required = false, defaultValue = "") String dataId,
                                 @RequestParam(required = false, defaultValue = "") String keyword) {
        checkLegalGroup(group);
        checkLegalProfile(profile);

        if (pageNo < 1) {
            pageNo = 1;
        }

        try {
            return JsonV2.successOf(configService.getProfileInfoPage(group, profile, dataId, keyword, (pageNo - 1) * pageSize, pageSize));
        } catch (RuntimeException e) {
            logger.error("get profile info error, group=[{}], profile=[{}], dataId=[{}], keyword=[{}]", group, profile, dataId, keyword, e);
            throw e;
        }
    }


    // 跨环境（子环境）拷贝文件
    @RequestMapping(value = "/copyTo", method = RequestMethod.POST)
    @ResponseBody
    public Object copyTo(@RequestBody CopyToDTO copyToDTO) {
        copyToDTO.setGroup(copyToDTO.getGroup());

        checkLegalGroup(copyToDTO.getGroup());
        checkLegalProfile(copyToDTO.getSrc());
        checkLegalProfile(copyToDTO.getProfile());

        ConfigMeta destConfigMeta = new ConfigMeta(copyToDTO.getGroup(), copyToDTO.getDataId(), copyToDTO.getProfile());
        Candidate destCandidate = configService.currentEdit(destConfigMeta);
        if (!canOverride(destCandidate)) {
            throw new RuntimeException("无法覆盖目标文件: 目标位置存在同名文件，并且是未发布状态");
        }

        ConfigMeta srcConfigMeta = new ConfigMeta(copyToDTO.getGroup(), copyToDTO.getDataId(), copyToDTO.getSrc());
        ConfigInfoWithoutPublicStatus srcConfig = configService.findPublishedConfigWithoutPublicStatus(srcConfigMeta);
        if (srcConfig == null) {
            throw new RuntimeException("源文件已经删除");
        }

        CandidateSnapshot snapshot = configService.getCandidateDetails(copyToDTO.getGroup(), copyToDTO.getDataId(), copyToDTO.getSrc(), srcConfig.getVersion());
        if (snapshot == null) {
            throw new RuntimeException("源文件已经删除");
        }
        long basedVersion = computeBasedVersion(destConfigMeta, destCandidate);
        CandidateDTO candidateDTO = new CandidateDTO(copyToDTO.getGroup(), copyToDTO.getDataId(), copyToDTO.getProfile(), basedVersion, basedVersion, StatusType.PENDING, snapshot.getData());

        //一个应用里，不同环境文件名相同，则使用的模板也相同(避免用晕)
        if (FileChecker.isTemplateFile(copyToDTO.getDataId())) {
            Optional<Map.Entry<String, String>> template = fileTemplateService.getTemplate(copyToDTO.getGroup(), copyToDTO.getDataId());
            if (template.isPresent()) {
                candidateDTO.setTemplateGroup(template.get().getKey());
                candidateDTO.setTemplate(template.get().getValue());
                candidateDTO.setDefaultConfigId(fileTemplateService.getDefaultConfigId(new ConfigMeta(copyToDTO.getGroup(), copyToDTO.getDataId(), copyToDTO.getProfile())));
            }
        }
        try {
            applyService.copyApply(candidateDTO, srcConfig, "拷贝文件");
        } catch (ModifiedException e) {
            throw new RuntimeException("配置文件已修改");
        } catch (StatusMismatchException e) {
            throw new RuntimeException("状态不匹配");
        } catch (ConfigExistException e) {
            throw new RuntimeException("目标配置已存在");
        } catch (TemplateChangedException e) {
            throw new RuntimeException("模板发生变化");
        } catch (IllegalTemplateException e) {
            throw new RuntimeException("服务器内部错误");
        }

        return JsonV2.successOf(true);
    }

    @RequestMapping(value = "/allowedCopyTo", method = RequestMethod.GET)
    @ResponseBody
    public Object getCopyToDestination(@RequestParam("group") String group, @RequestParam("profile") String profile) {
        checkLegalGroup(group);
        checkLegalProfile(profile);
        List<String> copyToList = generateCopyTo(group, profile);
        return JsonV2.successOf(null, copyToList);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Object delete(@RequestBody CandidateDTO dto) {
        logger.info("delete with {}", dto);
        checkArgument(dto.getStatus() == StatusType.PUBLISH || dto.getBasedVersion() == 0,
                "只有处于发布状态的版本文件或未发布过的文件才能被删除！");
        checkPublic(dto);
        Monitor.DELETE_STATICS.inc();
        try {
            applyService.delete(dto, "");
            return JsonV2.successOf(true);
        } catch (Exception e) {
            logger.warn("occur error when deleting file!, {}", dto);
            return handleException(dto, e);
        }
    }


    // 对“未发布”状态配置点击配置文件名字链接  //"/view/currentEdit"
    @RequestMapping(value = "/view/snapshot", method = RequestMethod.GET)
    @ResponseBody
    public Object currentEdit(@RequestParam("group") String group,
                              @RequestParam("dataId") String dataId,
                              @RequestParam("profile") String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            CandidateSnapshot snapshot = configService.currentEditSnapshot(meta);
            Map<String, Object> resultMap = getPreview(meta, snapshot.getEditVersion(), snapshot.getBasedVersion(),
                    snapshot.getStatus(), snapshot.getData());
            markIfFavorite(meta, resultMap);
            return JsonV2.successOf(null, resultMap);
        } catch (RuntimeException e) {
            logger.error("get current edit error, group=[{}], profile=[{}], dataId=[{}]", group, profile, dataId, e);
            throw e;
        }
    }

    // 对“已发布”状态配置点击配置文件名字链接
    @RequestMapping(value = "/view/currentPublishSnapshot", method = RequestMethod.GET)
    @ResponseBody
    public Object currentPublish(@RequestParam("group") String group,
                                 @RequestParam("dataId") String dataId,
                                 @RequestParam("profile") String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            long version = configService.currentVersionIncludeDeleted(meta);
            CandidateSnapshot snapshot = configService.getCandidateDetails(group, dataId, profile, version);
            checkArgument(snapshot.getStatus() == StatusType.PUBLISH,
                    "get published snapshot error, %s",
                    snapshot.getStatus());
            Map<String, Object> resultMap = getPreview(meta, version, snapshot.getBasedVersion(), StatusType.PUBLISH, snapshot.getData());
            //tole remove snapshot
            resultMap.put("snapshot", snapshot);
            resultMap.put("notRollback", true);
            markIfFavorite(meta, resultMap);
            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("get current publish error, group=[{}], profile=[{}], dataId=[{}]", group, profile, dataId, e);
            throw e;
        }
    }

    // 获取特定editVersion和version的snapshot  #"/view/editSnapshot"//与history/snapshot合并
    @RequestMapping(value = "view/editSnapshot", method = RequestMethod.GET)
    @ResponseBody
    public Object viewEditSnapshot(@RequestParam String group, @RequestParam String profile,
                                   @RequestParam String dataId, @RequestParam int editVersion) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            CandidateSnapshot snapshot = configService.getCandidateDetails(group, dataId, profile, editVersion);
            Map<String, Object> resultMap = getPreview(meta, snapshot.getEditVersion(), snapshot.getBasedVersion(),
                    snapshot.getStatus(), snapshot.getData());
            markIfFavorite(meta, resultMap);
            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("view edit snapshot error, group=[{}], profile=[{}], dataId=[{}], editVersion=[{}]", group,
                    profile, dataId, editVersion, e);
            throw e;
        }
    }


    // 在配置详情页面点击历史版本    不限制是否为PUBLISH状态的版本
    @RequestMapping(value = "/view/history/snapshot", method = RequestMethod.GET)
    @ResponseBody
    public Object getSnapshot(@RequestParam("group") String group, @RequestParam("profile") String profile,
                              @RequestParam("dataId") String dataId, @RequestParam("version") long version) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);

        try {
            CandidateSnapshot snapshot = configService.getCandidateDetails(group, dataId, profile, version);
            Map<String, Object> resultMap = getPreview(meta, snapshot.getEditVersion(), snapshot.getBasedVersion(),
                    snapshot.getStatus(), snapshot.getData());
            markIfFavorite(meta, resultMap);
            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("get snapshot error, group=[{}], profile=[{}], dataId=[{}], version=[{}]", group, profile,
                    dataId, version, e);
            throw e;
        }
    }


    // 判断能否回滚，在详情页点击“回滚至此版本”前会被调用
    @RequestMapping(value = "/canRollback", method = RequestMethod.POST)
    @ResponseBody
    public Object canRollback(@RequestBody FileMetaRequest fileMeta) {
        checkLegalMeta(fileMeta);

        try {
            Candidate candidate = configService.currentEdit(new ConfigMeta(fileMeta.getGroup(), fileMeta
                    .getDataId(), fileMeta.getProfile()));
            return JsonV2.successOf(candidate.getStatus() != StatusType.PASSED);
        } catch (RuntimeException e) {
            logger.error("ask can rollback error, {}", fileMeta, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    // 在详情页进行点击回滚至此版本，在调用前会先使用canRollback进行判断
    @RequestMapping(value = "/view/rollback/snapshot", method = RequestMethod.GET)
    @ResponseBody
    public Object rollback(@RequestParam("group") String group, @RequestParam("dataId") String dataId,
                           @RequestParam("profile") String profile, @RequestParam("version") long version) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        checkArgument(version > 0, "无效的 version");

        try {
            CandidateSnapshot oldSnapshot = configService.getCandidateDetails(group, dataId, profile, version);
            checkArgument(oldSnapshot.getStatus() == StatusType.PUBLISH, "无效的 version");

            Candidate candidate = configService.currentEdit(meta);
            return JsonV2.successOf(getPreview(meta, candidate.getEditVersion(), candidate.getBasedVersion(),
                    candidate.getStatus(), oldSnapshot.getData(), version));

        } catch (RuntimeException e) {
            logger.error("roll back error, group={}, dataId={}, profile={}, version={}", group, dataId, profile,
                    version, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    @RequestMapping("/view/reference/snapshot")
    @ResponseBody
    public Object viewReference(@RequestParam("group") String group, @RequestParam("dataId") String dataId,
                                @RequestParam("profile") String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            ConfigMeta reference = referenceService.findReference(meta);
            if (reference == null) {
                logger.warn("can not get reference, group=[{}], profile=[{}], dataId=[{}]", group, profile, dataId);
                throw new IllegalArgumentException();
            }
            long lastVersion = configService.currentVersionIncludeDeleted(reference);
            CandidateSnapshot snapshot = configService.getCandidateDetails(reference.getGroup(), reference.getDataId(),
                    reference.getProfile(), lastVersion);
            checkArgument(snapshot.getStatus() == StatusType.PUBLISH, "get published snapshot error, %s",
                    snapshot.getStatus());
            Map<String, Object> resultMap = getPreview(reference, lastVersion, snapshot.getBasedVersion(), StatusType.PUBLISH,
                    snapshot.getData());
            resultMap.put("isViewReference", true);
            markIfFavorite(meta, resultMap);
            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("view reference error, group=[{}], profile=[{}], dataId=[{}]", group, profile, dataId, e);
            throw e;
        }
    }

    @RequestMapping("/getPropertiesTemplate")
    @ResponseBody
    public Object getPropertiesTemplate(@RequestParam String group, @RequestParam String dataId, @RequestParam String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            Optional<String> template = fileTemplateService.getPropertiesTemplate(meta);
            return JsonV2.successOf(template.orElse(""));
        } catch (Exception e) {
            logger.error("get properties template error, {}", meta, e);
            return new JsonV2<>(1, "获取properties模版错误", null);
        }
    }

    @RequestMapping("/versions")
    @ResponseBody
    public Object getVersions(@RequestBody FileMetaRequest fileMeta) {
        ConfigMeta meta = transform(fileMeta);
        checkLegalMeta(meta);
        List<VersionDetail> versions = candidateSnapshotDao.findVersionsDetailWithStatus(meta, StatusType.PUBLISH);
        // TODO 加在这里会展示在侧边栏，最好给个标识
        long lastPublishVersion = configService.getCurrentPublishedData(meta).getVersion();
        List<VersionDetail> unPublishVersion = candidateSnapshotDao.findVersionsDetailWithBegin(meta,
                (int) lastPublishVersion);

        versions.addAll(unPublishVersion);

        Map<Long, String> comments = fileCommentService.getComments(meta);
        for (VersionDetail versionDetail : versions) {
            versionDetail.setDescription(comments.get(versionDetail.getVersion()));
        }
        List<VersionDetail> versionList = Ordering.natural().reverse().immutableSortedCopy(versions);
        return JsonV2.successOf(null, versionList);
    }

    @RequestMapping("/description")
    @ResponseBody
    public Object getFileDescription(@RequestParam String group, @RequestParam String dataId) {
        String description = fileDescriptionService.getDescription(group, dataId);
        return JsonV2.successOf("", description);
    }

    @RequestMapping("/description/update")
    @ResponseBody
    public Object setFileDescription(@RequestBody FileDescriptionVo vo) {
        checkLegalGroup(vo.getGroup());
        checkLegalDataId(vo.getDataId());
        String description = vo.getDescription();
        checkArgument(description != null && description.length() <= AdminConstants.MAX_DESC_LENGTH, "文件描述不能超过150字符");
        fileDescriptionService.setDescription(vo.getGroup(), vo.getDataId(), description);
        return JsonV2.success();
    }

    @RequestMapping("/comment")
    @ResponseBody
    public Object getFileComment(String group, String dataId, String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        List<CommentVo> list = fileCommentService.getCommentsByMeta(meta);
        return JsonV2.successOf(Ordering.from((Comparator<CommentVo>) (o1, o2) -> Longs.compare(o2.getVersion(), o1.getVersion())).sortedCopy(list));
    }

    @RequestMapping("/permission")
    @ResponseBody
    public Object getFilePermission(@RequestParam("group") String group,
                                    @RequestParam("dataId") String dataId,
                                    @RequestParam("profile") String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        boolean canEdit = permissionService.hasFilePermission(group, profile, dataId, PermissionType.EDIT);
        boolean canApprove = permissionService.hasFilePermission(group, profile, dataId, PermissionType.APPROVE);
        boolean canPublish = permissionService.hasFilePermission(group, profile, dataId, PermissionType.PUBLISH);
        boolean canGroupEdit = permissionService.hasPermission(group, Environment.RESOURCES.defaultProfile(), PermissionType.EDIT);
        boolean canGroupApprove = permissionService.hasPermission(group, Environment.RESOURCES.defaultProfile(), PermissionType.APPROVE);
        boolean canGroupPublish = permissionService.hasPermission(group, Environment.RESOURCES.defaultProfile(), PermissionType.PUBLISH);
        Map<String, Boolean> permissionMap = Maps.newHashMap();
        permissionMap.put("canEdit", canEdit);
        permissionMap.put("canApprove", canApprove);
        permissionMap.put("canPublish", canPublish);
        permissionMap.put("canGroupEdit", canGroupEdit);
        permissionMap.put("canGroupApprove", canGroupApprove);
        permissionMap.put("canGroupPublish", canGroupPublish);
        //tole 仔细看下逻辑，一直true
        return JsonV2.successOf(null, permissionMap);
    }

    // 用于判断当前是否有人编辑过
    @RequestMapping(value = "/view/allowEdit", method = RequestMethod.POST)
    @ResponseBody
    public Object currentPageStatus(@RequestBody CandidateDTO candidateDTO) {
        if (Strings.isNullOrEmpty(candidateDTO.getDataId())) {
            return JsonV2.successOf(PageStatus.CAN_EDIT);
        }
        checkLegalMeta(candidateDTO);

        try {
            ConfigMeta configMeta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(),
                    candidateDTO.getProfile());
            long publishVersion = configService.currentVersionIncludeDeleted(configMeta);
            Candidate currentEdit = configService.currentEdit(configMeta);
            return JsonV2.successOf(currentPageStatus(candidateDTO.getEditVersion(), currentEdit, publishVersion));
        } catch (RuntimeException e) {
            logger.error("get current edit version error, {}", candidateDTO, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    @RequestMapping(value = "/types")
    @ResponseBody
    public Object allowedFileTypes() {
        return JsonV2.successOf(qConfigFileTypeService.findAllQConfigFileTypes());
    }


    @RequestMapping(value = "/exists", method = RequestMethod.POST)
    @ResponseBody
    public Object fileExists(@RequestBody FileMetaRequest fileMeta) {
        String group = fileMeta.getGroup();
        String dataId = fileMeta.getDataId();
        String profile = fileMeta.getProfile();
        if (Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId) || Strings.isNullOrEmpty(profile)) {
            return JsonV2.failOf("group=[" + group + "], dataId=[" + dataId + "], profile=['" + profile + "'] + 为空");
        }
        if (configService.existWithoutStatus(group, profile, dataId, StatusType.DELETE)) {
            return JsonV2.failOf("文件：" + dataId + "已经存在");
        }
        return JsonV2.successOf("newFile");
    }

    @RequestMapping(value = "/existsPublicFile", method = RequestMethod.POST)
    @ResponseBody
    public Object publicFileExists(@RequestBody FileMetaRequest fileMeta) {
        String group = fileMeta.getGroup();
        String dataId = fileMeta.getDataId();
        String profile = fileMeta.getProfile();
        if (Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId) || Strings.isNullOrEmpty(profile)) {
            return new JsonV2<>(-1, "group=[" + group + "], dataId=[" + dataId + "], profile=['" + profile + "'] + 为空", null);
        }
        Optional<String> checkExistsPublicFile = configService.checkWithoutPublicFile(profile, dataId);
        if(checkExistsPublicFile.isPresent()){
            return JsonV2.failOf(checkExistsPublicFile.get());
        }
        return JsonV2.successOf("newFile");
    }


    // 在列表页面点击非“已发布”的版本的“预览”按钮  点击删除时先加载这个
    @RequestMapping(value = "/view/viewCompleteData", method = RequestMethod.POST)
    @ResponseBody
    public Object viewCompleteData(@RequestBody CandidateDTO candidate) {
        return JsonV2.successOf(getCompleteData(candidate));
    }

    @RequestMapping(value = "/view/isRealPropertiesFile", method = RequestMethod.POST)
    @ResponseBody
    public Object isRealPropertiesFile(@RequestBody CandidateDTO dto) {
        checkLegalGroup(dto.getGroup());
        checkLegalDataId(dto.getDataId());
        return propertiesCheckService.isRealPropertyFile(dto.getGroup(), dto.getDataId(), dto.getData());
    }


    @RequestMapping(value = "/view/isParentFileRealPropertiesFile", method = RequestMethod.POST)
    @ResponseBody
    public Object isParentFileRealPropertiesFile(@RequestBody CandidateDTO dto) {
        checkLegalGroup(dto.getInheritGroupId());
        checkLegalDataId(dto.getInheritDataId());
        return propertiesCheckService.isRealPropertyFile(dto.getInheritGroupId(), dto.getInheritDataId(), dto.getInheritData());
    }


    private Map<String, Object> getPreview(ConfigMeta configMeta, long editVersion, long basedVersion,
                                           StatusType status, String data) {
        return getPreview(configMeta, editVersion, basedVersion, status, data, editVersion);
    }


    private Map<String, Object> getPreview(ConfigMeta configMeta, long editVersion, long basedVersion,
                                           StatusType status, String data, long loadVersion) {
        VersionData<Config> versionData = VersionData.of(editVersion,
                new Config(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), data));
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("versions", candidateSnapshotDao.findCandidateSnapshots(configMeta, StatusType.PUBLISH, 20));
        resultMap.put("versionData", versionData);
        resultMap.put("loadVersion", String.valueOf(loadVersion));
        resultMap.put("basedVersion", basedVersion);
        resultMap.put("env", Environment.fromProfile(configMeta.getProfile()).text());
        resultMap.put("buildGroup", ProfileUtil.getBuildGroup(configMeta.getProfile()));
        resultMap.put("description", fileDescriptionService.getDescription(configMeta.getGroup(), configMeta.getDataId()));
        resultMap.put("configOpLogs", configOpLogDao.selectRecent(configMeta, basedVersion, configLogLength));

        /*
         * 处理继承关系数据
         */
        InheritConfigMeta inheritConfigMeta = new InheritConfigMeta();
        inheritConfigMeta.setGroupId(configMeta.getGroup());
        inheritConfigMeta.setDataId(configMeta.getDataId());
        inheritConfigMeta.setProfile(configMeta.getProfile());
        inheritConfigMeta = inheritConfigService.queryInheritInfoDetail(inheritConfigMeta);

        if (inheritConfigMeta != null && !Strings.isNullOrEmpty(inheritConfigMeta.getInheritGroupId())
                && !Strings.isNullOrEmpty(inheritConfigMeta.getInheritDataId())) {
            resultMap.put("inherit", inheritConfigMeta.getInheritDataId());
            resultMap.put("inheritGroup", inheritConfigMeta.getInheritGroupId());
            resultMap.put("inheritProfile", inheritConfigMeta.getInheritProfile());
            ConfigMeta tmpMeta = new ConfigMeta(inheritConfigMeta.getInheritGroupId(), inheritConfigMeta.getInheritDataId(), inheritConfigMeta.getInheritProfile());
            long version = configService.currentVersionIncludeDeleted(tmpMeta);
            resultMap.put("inheritDesc", fileDescriptionService.getDescription(inheritConfigMeta.getInheritGroupId(), inheritConfigMeta.getInheritDataId()));
            CandidateSnapshot inheritSnapshot = configService.getCandidateDetails(tmpMeta.getGroup(), tmpMeta.getDataId(), tmpMeta.getProfile(), version);
            if (inheritSnapshot != null && !Strings.isNullOrEmpty(inheritSnapshot.getData())) {
                inheritSnapshot.setData(inheritSnapshot.getData());
            }
            resultMap.put("inheritSnapshot", inheritSnapshot);
        } else {
            resultMap.put("inherit", "");//用来提醒前端这不是继承关系的配置
            resultMap.put("inheritGroup", "");
        }

        if (FileChecker.isTemplateFile(configMeta.getDataId())) {
            Optional<Map.Entry<String, String>> templateEntry = fileTemplateService.getTemplate(configMeta.getGroup(), configMeta.getDataId());
            if (templateEntry.isPresent()) {
                resultMap.put("templateGroup", templateEntry.get().getKey());
                resultMap.put("template", templateEntry.get().getValue());
                resultMap.put("allowBatchOp", tableBatchOpWhitelistService.allowBatchOp(configMeta.getGroup()));
                resultMap.put("templateVersion", fileTemplateService.getFileMappingTemplateCurrentVersion(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), editVersion));
            }
        } else if (configMeta.getDataId().endsWith(".json")) {
            Optional<Map.Entry<String, String>> templateEntry = fileTemplateService.getTemplate(configMeta.getGroup(), configMeta.getDataId());
            if (templateEntry.isPresent()) {
                resultMap.put("templateGroup", templateEntry.get().getKey());
                resultMap.put("template", templateEntry.get().getValue());
                resultMap.put("templateType", "json");
                resultMap.put("templateVersion", fileTemplateService.getFileMappingTemplateCurrentVersion(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), editVersion));
            } else {
                resultMap.put("templateGroup", "");
                resultMap.put("template", "");
                resultMap.put("templateVersion", "");
            }
        } else {
            resultMap.put("templateGroup", "");
            resultMap.put("template", "");
        }
        resultMap.put("defaultConfigId", fileTemplateService.getDefaultConfigId(configMeta));
        resultMap.put("configEditorSettings", configEditorSettingsService.settingsOf(configMeta.getGroup(), configMeta.getDataId()));

        boolean canEdit = permissionService.hasFilePermission(configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), PermissionType.EDIT);
        resultMap.put("canEdit", canEdit);
        boolean canApprove = permissionService.hasFilePermission(configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), PermissionType.APPROVE);
        resultMap.put("canApprove", canApprove);
        boolean canPublish = permissionService.hasFilePermission(configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), PermissionType.PUBLISH);
        resultMap.put("canPublish", canPublish);

        resultMap.put("canGroupEdit", permissionService.hasPermission(configMeta.getGroup(), Environment.RESOURCES.defaultProfile(), PermissionType.EDIT));
        resultMap.put("canGroupApprove", permissionService.hasPermission(configMeta.getGroup(), Environment.RESOURCES.defaultProfile(), PermissionType.APPROVE));
        resultMap.put("canGroupPublish", permissionService.hasPermission(configMeta.getGroup(), Environment.RESOURCES.defaultProfile(), PermissionType.PUBLISH));
        resultMap.put("status", status);
        ConfigInfoWithoutPublicStatus publishedConfig = configService.findPublishedConfigWithoutPublicStatus(configMeta);
        if (publishedConfig == null) {
            resultMap.put("isPublic", null);
            resultMap.put("isInherit", null);
            resultMap.put("isReference", null);
            resultMap.put("isRest", null);
            resultMap.put("isInuse", false);
        } else {
            ConfigMetaWithoutProfile metaWithoutProfile = new ConfigMetaWithoutProfile(configMeta);
            resultMap.put("isPublic", filePublicStatusService.isPublic(metaWithoutProfile));
            resultMap.put("isInherit", filePublicStatusService.isInherit(metaWithoutProfile));
            resultMap.put("isReference", filePublicStatusService.isReference(metaWithoutProfile));
            resultMap.put("isRest", filePublicStatusService.isRest(metaWithoutProfile));
            resultMap.put("isInuse", true);
        }
        return resultMap;
    }

    //tole 以后环境可自定义，不特殊处理prod环境
    private List<String> generateCopyTo(String group, String profile) {
        Set<Environment> environments = userContextService.getEnvironments(group);
        Environment currentEnv = Environment.fromProfile(profile);
        List<String> result = new ArrayList<>();
        for (Environment env : environments) {
            if (!env.isResources() && !env.equals(currentEnv)) {
                result.add(env.profile());
            }
        }
        Collections.sort(result);
        return result;
    }

    private boolean canOverride(Candidate destCandidate) {
        return destCandidate == null
                || destCandidate.getStatus() == StatusType.PUBLISH
                || destCandidate.getStatus() == StatusType.DELETE;
    }

    private long computeBasedVersion(ConfigMeta destConfigMeta, Candidate destCandidate) {
        ConfigInfoWithoutPublicStatus destConfig = configService.findPublishedConfigWithoutPublicStatus(destConfigMeta);
        if (destConfig == null) {
            if (destCandidate == null) return 0;
            return destCandidate.getStatus() == StatusType.DELETE ? destCandidate.getEditVersion() : 0;
        }
        return destConfig.getVersion();
    }

    private void checkPublic(CandidateDTO dto) {
        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        boolean isPublic = filePublicStatusService.isPublic(new ConfigMetaWithoutProfile(dto.getGroup(), dto.getDataId()));
        if (!isPublic) return;
        checkArgument(referenceService.beReferenceCount(meta) == 0, "该文件正在被别的应用引用，无法删除！");
    }


    private Object currentPageStatus(long editVersion, Candidate currentEdit, long publishVersion) {
        if (currentEdit == null) {
            return PageStatus.CAN_EDIT;
        } else {
            return currentPageStatus(editVersion, publishVersion, currentEdit.getEditVersion(), currentEdit.getStatus());
        }
    }


    private Object currentPageStatus(long editVersion, long publishVersion, long currentEditVersion, StatusType currentEditStatus) {
        if (currentEditStatus == StatusType.PASSED) {
            return PageStatus.FIXED;
        } else if (currentEditStatus == StatusType.DELETE) {
            return PageStatus.CAN_EDIT;
        } else if (currentEditVersion == editVersion) {
            return PageStatus.CAN_EDIT;
        } else if (currentEditVersion > editVersion && publishVersion <= editVersion) {
            return PageStatus.CAN_LOAD;
        } else {
            return PageStatus.FIXED;
        }
    }

    private Object getCompleteData(CandidateDTO candidate) {
        checkLegalMeta(candidate);

        try {
            Map<String, Object> map = Maps.newHashMap();

            CandidateSnapshot candidateDetails = configService.getCandidateDetails(
                    candidate.getGroup(),
                    candidate.getDataId(),
                    candidate.getProfile(),
                    candidate.getEditVersion());

            String templateDetail = "";
            if (FileChecker.isTemplateFile(candidate.getDataId())) {
                Optional<Map.Entry<String, String>> templateEntry = fileTemplateService.getTemplate(candidate.getGroup(), candidate.getDataId());
                if (templateEntry.isPresent()) {
                    Optional<String> detail = fileTemplateService.getTemplateDetail(templateEntry.get().getKey(), templateEntry.get().getValue());
                    if (detail.isPresent()) {
                        templateDetail = detail.get();

                        Optional<String> optional = TemplateUtils.processTimeLongToStr(candidate.getDataId(), candidateDetails.getData(), templateDetail);
                        optional.ifPresent(candidateDetails::setData);
                    }
                }
            }

            map.put("data", candidateDetails);
            map.put("templateDetail", templateDetail);
            map.put("log", configOpLogDao.selectRecent(new ConfigMeta(candidate.getGroup(), candidate.getDataId(),
                    candidate.getProfile()), candidateDetails.getBasedVersion(), configLogLength));

            return map;
        } catch (RuntimeException e) {
            logger.error("get complete data error, {}", candidate, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    private void markIfFavorite(ConfigMeta meta, Map<String, Object> map) {
        map.put("favoriteFile", userBehaviorService.isFavoriteFile(meta, userContext.getRtxId()));
    }
}
