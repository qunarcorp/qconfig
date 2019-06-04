package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.ActionVo;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaRequest;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.dto.FileRollbackDTO;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateType;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.CheckKeyConflictService;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.EventPostApplyService;
import qunar.tc.qconfig.admin.service.FileDescriptionService;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.impl.CheckEnvConflictServiceImpl;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Controller
@RequestMapping("/qconfig/file")
public class ActionController extends AbstractControllerHelper {

    private static Logger logger = LoggerFactory.getLogger(ActionController.class);

    @Resource
    private EventPostApplyService applyService;

    @Resource
    private ProfileService profileService;

    @Resource
    private FileTemplateService templateService;

    @Resource
    private ConfigService configService;

    @Resource
    private CheckKeyConflictService checkKeyConflictService;

    @Resource
    private CheckEnvConflictServiceImpl checkEnvConflictService;

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private ConfigEditorSettingsService configEditorSettingsService;

    @Resource
    private FileDescriptionService fileDescriptionService;


    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    @ResponseBody
    public Object apply(@RequestBody CandidateDTO candidate,
                        @RequestParam(value = "isForceApply", required = false, defaultValue = "false") boolean isForceApply) {
        logger.info("apply with {}", candidate);
        //tole 兼容前端空模板传[]
        if ("[]".equals(candidate.getTemplateDetail())) {
            candidate.setTemplateDetail("");
        }
        checkApply(candidate);
        try {
            if (isForceApply) {
                Monitor.FORCE_APPLY_STATICS.inc();
                applyService.forceApply(candidate, "");
            } else {
                Monitor.APPLY_STATICS.inc();
                applyService.apply(candidate, "");
            }
            return JsonV2.success();
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    @RequestMapping(value = "/forceCreate", method = RequestMethod.POST)
    @ResponseBody
    public Object forceApply(@RequestBody CandidateDTO candidate) {
        checkApply(candidate);
        Monitor.FORCE_APPLY_STATICS.inc();
        try {
            applyService.forceApply(candidate, "");
            return JsonV2.success();
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    @ResponseBody
    public Object approve(@RequestBody CandidateDTO candidate) {
        logger.info("approve with {}", candidate);
        checkCandidate(candidate);
        Monitor.APPROVE_STATICS.inc();
        try {
            applyService.approve(candidate, "");
            return JsonV2.success();
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    @RequestMapping(value = "/reject", method = RequestMethod.POST)
    @ResponseBody
    public Object reject(@RequestBody CandidateDTO candidate) {
        logger.info("reject with {}", candidate);
        checkCandidate(candidate);
        Monitor.REJECT_STATICS.inc();
        try {
            applyService.reject(candidate, "");
            return JsonV2.success();
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    @ResponseBody
    public Object publish(@RequestBody CandidateDTO candidate) {
        logger.info("publish with {}", candidate);
        checkCandidate(candidate);
        Monitor.PUBLISH_STATICS.inc();
        try {
            applyService.publish(candidate, "");
            return JsonV2.success();
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    @RequestMapping(value = "/oneButtonPublish", method = RequestMethod.POST)
    @ResponseBody
    public Object oneButtonPublish(@RequestBody CandidateDTO dto,
                                   @RequestParam(value = "isForceApply", required = false, defaultValue = "false") boolean isForceApply) {
        logger.info("one button publish with {}", dto);
        //tole 兼容前端空模板传[]
        if ("[]".equals(dto.getTemplateDetail())) {
            dto.setTemplateDetail("");
        }
//        Preconditions.checkArgument(Environment.fromProfile(dto.getProfile()) == Environment.DEV,
//                "only dev environment can use one button publish");
        checkApply(dto);
        Monitor.ONE_BUTTON_PUBLISH_STATICS.inc();
        try {
            applyService.oneButtonPublish(dto, "", isForceApply);
            return JsonV2.success();
        } catch (Exception e) {
            logger.warn("occur error in one button publish, {}", dto);
            return handleException(dto, e);
        }
    }

    private void checkApply(CandidateDTO candidate) {
        checkCandidate(candidate);
        String buildGroup = ProfileUtil.getBuildGroup(candidate.getProfile());
        if (!Strings.isNullOrEmpty(buildGroup)) {
            checkArgument(profileService.exist(candidate.getGroup(), candidate.getProfile()),
                    "group [%s] 没有profile [%s]", candidate.getGroup(), candidate.getProfile());
        }

        checkArgument(!Strings.isNullOrEmpty(candidate.getData() + " "), "文件内容不能为空");

        checkTemplate(candidate);
    }

    private void checkCandidate(CandidateDTO candidate) {
        checkLegalMeta(candidate);
        checkArgument(candidate.getBasedVersion() >= 0, "无效的 based version");
        checkLegalEditVersion(candidate.getEditVersion());

        FileChecker.checkName(candidate.getDataId());

        String buildGroup = ProfileUtil.getBuildGroup(candidate.getProfile());
        checkArgument(buildGroup.length() <= Environment.BUILD_GROUP_MAX_LENGTH, "%s长度不能大于%s个字符", QConfigAttributesLoader.getInstance().getBuildGroup(), Environment.BUILD_GROUP_MAX_LENGTH);
        checkArgument(ProfileUtil.BUILD_GROUP_LETTER_DIGIT_PATTERN.matcher(buildGroup).find(), "%s不能包含[字符，数字，'_'，'-']以外的其它内容", QConfigAttributesLoader.getInstance().getBuildGroup());

        // todo: 好几处地方有长度限制了，得弄一下
        checkArgument(candidate.getData() == null || candidate.getData().getBytes(Charsets.UTF_8).length < AdminConstants.MAX_FILE_SIZE_IN_K * 1024, "文件大小不能超过%sk", AdminConstants.MAX_FILE_SIZE_IN_K);

        checkArgument(candidate.getDescription() == null || candidate.getDescription().length() <= AdminConstants.MAX_DESC_LENGTH, "文件描述长度不能超过%s个字", AdminConstants.MAX_DESC_LENGTH);
    }


    private void checkTemplate(CandidateDTO candidate) {
        if (FileChecker.isTemplateFile(candidate.getDataId())) {
            checkArgument(!Strings.isNullOrEmpty(candidate.getTemplateGroup()) && !Strings.isNullOrEmpty(candidate.getTemplate()), ".t后缀代表模版文件，必须采用模版");
        }

        if (!Strings.isNullOrEmpty(candidate.getTemplateGroup()) && !Strings.isNullOrEmpty(candidate.getTemplate())) {
            final Optional<TemplateInfo> info = templateService.getTemplateInfo(candidate.getTemplateGroup(), candidate.getTemplate());
            if (info.isPresent()) {
                final TemplateType type = info.get().getType();
                if (type == TemplateType.JSON_SCHEMA) {
                    checkArgument(candidate.getDataId().endsWith(".json"), "使用了JSON模板，文件后缀必须是.json");
                } else if (type == TemplateType.TABLE) {
                    checkArgument(candidate.getDataId().endsWith(".t"), "使用了表格模板，文件后缀必须是.t");
                }
            }
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public Object cancel(@RequestBody CandidateDTO candidate) {
        logger.info("cancel with {}", candidate);
        checkCandidate(candidate);
        Monitor.RETURN_APPROVE_STATICS.inc();
        try {
            applyService.cancel(candidate, "");
            return JsonV2.success();
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    @RequestMapping(value = "/check/keyConflict", method = RequestMethod.POST)
    @ResponseBody
    public Object checkKeyConflict(@RequestBody CandidateDTO candidate) {
        checkLegalMeta(new ConfigMeta(candidate.getGroup(), candidate.getDataId(), candidate.getProfile()));
        checkLegalEditVersion(candidate.getEditVersion());

        if (!checkKeyConflictService.needCheck(candidate.getDataId())) {
            return JsonV2.successOf(ImmutableList.of());
        }

        CandidateSnapshot snapshot;
        if (Strings.isNullOrEmpty(candidate.getData())) {
            snapshot = configService.getCandidateDetails(candidate.getGroup(), candidate.getDataId(), candidate.getProfile(), candidate.getEditVersion());
        } else {
            snapshot = new CandidateSnapshot(candidate.getGroup(), candidate.getDataId(), candidate.getProfile(), candidate.getBasedVersion(), candidate.getEditVersion(), candidate.getData(), "", null);
        }

        if (snapshot == null) {
            return new JsonV2<Object>(ResultStatus.ILLEGAL_FILE.code(), "文件内容为空或者文件不存在: " + candidate.getGroup() + ", " + candidate.getDataId() + ", " + candidate.getProfile() + ", " + candidate.getEditVersion(), "");
        }

        try {
            return JsonV2.successOf(doCheckKeyConflict(snapshot));
        } catch (Exception e) {
            return handleException(candidate, e);
        }
    }

    private Object doCheckKeyConflict(CandidateSnapshot snapshot) {
        List<PropertyConflict> conflictsView = Lists.newArrayList();
        Multimap<String, ConfigMeta> conflicts = checkKeyConflictService.checkKeyConflict(snapshot);
        for (Map.Entry<String, Collection<ConfigMeta>> entry : conflicts.asMap().entrySet()) {
            conflictsView.add(new PropertyConflict(entry.getKey(), ImmutableList.copyOf(entry.getValue())));
        }
        Collections.sort(conflictsView);
        return conflictsView;
    }

    @RequestMapping(value = "/check/refKeyConflict", method = RequestMethod.POST)
    @ResponseBody
    public Object checkRefKeyConflict(@RequestBody Reference reference) {
        if (StringUtils.isEmpty(reference.getAlias())) {
            reference.setAlias(reference.getRefDataId());
        }
        ConfigMeta aliasMeta = new ConfigMeta(reference.getGroup(), reference.getAlias(), reference.getProfile());
        ConfigMeta refMeta = new ConfigMeta(reference.getRefGroup(), reference.getRefDataId(), reference.getRefProfile());
        checkLegalMeta(aliasMeta);
        checkLegalMeta(refMeta);

        if (!checkKeyConflictService.needCheck(aliasMeta.getDataId())) {
            return ImmutableList.of();
        }

        ConfigInfoWithoutPublicStatus refCurrent = configService.findPublishedConfigWithoutPublicStatus(refMeta);
        if (refCurrent == null) {
            return new JsonV2<Object>(ResultStatus.ILLEGAL_FILE.code(), "文件内容为空或者文件不存在: " + refMeta.getGroup() + ", " + refMeta.getDataId() + ", " + refMeta.getProfile(), "");
        }

        CandidateSnapshot refSnapshot = configService.getCandidateDetails(refMeta.getGroup(), refMeta.getDataId(), refMeta.getProfile(), refCurrent.getVersion());
        CandidateSnapshot snapshot = new CandidateSnapshot(aliasMeta.getGroup(), aliasMeta.getDataId(),
                aliasMeta.getProfile(), refSnapshot.getBasedVersion(), refSnapshot.getEditVersion(),
                refSnapshot.getData(), refSnapshot.getOperator(), refSnapshot.getStatus());

        try {
            return doCheckKeyConflict(snapshot);
        } catch (Exception e) {
            logger.error("check key conflict error, {}", reference, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }


    // 在详情页面点击开放文件
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    @ResponseBody
    public Object makePublic(@RequestBody FileMetaRequest fileMeta) {
        ConfigMeta configMeta = transform(fileMeta);
        logger.info("make public {}", configMeta);
        Monitor.PUBLIC_STATICS.inc();
        try {
            applyService.makePublic(configMeta, "");
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("make public error, {}", configMeta, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    // 设置为继承
    @RequestMapping(value = "/makeInherit", method = RequestMethod.POST)
    @ResponseBody
    public Object makeInherit(@RequestBody CandidateDTO candidateDTO) {
        ConfigMeta configMeta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(),
                candidateDTO.getProfile());
        logger.info("make inherit {}", configMeta);
        Monitor.INHERIT_STATICS.inc();
        Optional<Conflict> conflict = checkEnvConflictService.parentFileExistsInOtherGroup(new ConfigMeta(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile()));
        checkArgument(!conflict.isPresent(), "其他应用已经使用" + configMeta.getDataId() + " 作为父文件名称！");
        try {
            applyService.makeInherit(configMeta, "");
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("make inherit error, {}", configMeta, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    // 设置为Rest
    @RequestMapping(value = "/makeRest", method = RequestMethod.POST)
    @ResponseBody
    public Object makeRest(@RequestBody CandidateDTO candidateDTO) {
        ConfigMeta configMeta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(),
                candidateDTO.getProfile());
        logger.info("make rest {}", configMeta);
        try {
            applyService.makeRest(configMeta, "");
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("make inherit error, {}", configMeta, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    // 新建配置文件按钮
    @RequestMapping(value = "/edit/applyRender", method = RequestMethod.GET)
    @ResponseBody
    public Object applyRender(@RequestParam("group") String group,
                              @RequestParam("profile") String profile,
                              @RequestParam(value = "dataId", required = false) String dataId,
                              @RequestParam(value = "template", defaultValue = "") String template,
                              @RequestParam(value = "templateGroup", defaultValue = "") String templateGroup,
                              @RequestParam(value = "defaultConfigId", required = false, defaultValue = "0") long defaultConfigId,
                              @RequestParam(value = "validateUrl", defaultValue = "") String validateUrl,
                              @RequestParam(value = "inherit", defaultValue = "") String inherit,
                              @RequestParam(value = "inheritGroup", defaultValue = "") String inheritGroup,
                              @RequestParam(value = "inheritProfile", defaultValue = "") String inheritProfile) {
        checkLegalGroup(group);
        checkLegalProfile(profile);
        Map<String, Object> resultMap = Maps.newHashMap();
        ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
        VersionData<ConfigMeta> versionData = new VersionData<>(INIT_BASED_VERSION, configMeta);

        resultMap.put("versions", null);
        resultMap.put("versionData", versionData);
        resultMap.put("basedVersion", INIT_BASED_VERSION);
        resultMap.put("env", Environment.fromProfile(profile).text());
        resultMap.put("buildGroup", ProfileUtil.getBuildGroup(profile));
        resultMap.put("configOpLogs", Collections.emptyList());
        resultMap.put("canGroupEdit", permissionService.hasPermission(group, Environment.RESOURCES.defaultProfile(), PermissionType.EDIT));
        resultMap.put("canGroupApprove", permissionService.hasPermission(group, Environment.RESOURCES.defaultProfile(), PermissionType.APPROVE));
        resultMap.put("canGroupPublish", permissionService.hasPermission(group, Environment.RESOURCES.defaultProfile(), PermissionType.PUBLISH));
        resultMap.put("defaultConfigId", defaultConfigId);
        resultMap.put("validateUrl", validateUrl);
        resultMap.put("inherit", inherit);
        resultMap.put("inheritGroup", inheritGroup);
        resultMap.put("inheritProfile", inheritProfile);
        resultMap.put("configEditorSettings", configEditorSettingsService.settingsOf(configMeta.getGroup(), configMeta.getDataId()));
        resultMap.put("isPublic", null);
        resultMap.put("isInherit", null);
        resultMap.put("isReference", null);
        resultMap.put("isRest", null);
        if (!Strings.isNullOrEmpty(inherit)
                && CheckUtil.isLegalGroup(inheritGroup)
                && !Strings.isNullOrEmpty(inheritProfile)) {
            dealInherit(resultMap, inheritGroup, inherit, inheritProfile);//处理继承相关数据
        }

        final Optional<TemplateInfo> templateInfo = templateService.getTemplateInfo(templateGroup, template);
        if (templateInfo.isPresent() && templateInfo.get().getType() == TemplateType.JSON_SCHEMA) {
            resultMap.put("templateType", "json");
        }

        if (Strings.isNullOrEmpty(dataId)) {
            resultMap.put("templateGroup", templateGroup);
            resultMap.put("template", template);
            resultMap.put("description", "");
            boolean canEdit = permissionService.hasPermission(group, profile, PermissionType.EDIT);
            resultMap.put("canEdit", canEdit);
            boolean canApprove = permissionService.hasPermission(group, profile, PermissionType.APPROVE);
            resultMap.put("canApprove", canApprove);
            boolean canPublish = permissionService.hasPermission(group, profile, PermissionType.PUBLISH);
            resultMap.put("canPublish", canPublish);
        } else {
            resultMap.put("templateGroup", templateGroup);
            resultMap.put("template", template);
            resultMap.put("description", fileDescriptionService.getDescription(group, dataId));
            boolean canEdit = permissionService.hasFilePermission(group, profile, dataId, PermissionType.EDIT);
            resultMap.put("canEdit", canEdit);
            boolean canApprove = permissionService.hasFilePermission(group, profile, dataId, PermissionType.APPROVE);
            resultMap.put("canApprove", canApprove);
            boolean canPublish = permissionService.hasFilePermission(group, profile, dataId, PermissionType.PUBLISH);
            resultMap.put("canPublish", canPublish);
        }
        return JsonV2.successOf(resultMap);
    }

    /**
     * 处理继承相关的数据
     */
    private void dealInherit(Map<String, Object> resultMap, String inheritGroupId, String inheritDataId, String inheritProfile) {
        ConfigMeta meta = new ConfigMeta(inheritGroupId, inheritDataId, inheritProfile);
        long version = configService.currentVersionIncludeDeleted(meta);
        CandidateSnapshot inheritSnapshot = configService.getCandidateDetails(inheritGroupId, inheritDataId, inheritProfile, version);
        if (inheritSnapshot != null && !Strings.isNullOrEmpty(inheritSnapshot.getData())) {
            inheritSnapshot.setData(inheritSnapshot.getData());
        }
        resultMap.put("inheritSnapshot", inheritSnapshot);
        resultMap.put("inheritDesc", fileDescriptionService.getDescription(inheritGroupId, inheritDataId));
    }

    /**
     * 回滚文件
     *
     * @param dto 文件详情
     * @return 成功返回没有msg
     */
    @RequestMapping(value = "/rollback", method = RequestMethod.POST)
    @ResponseBody
    public Object rollBack(@RequestBody FileRollbackDTO dto,
            @RequestParam(value = "isApprove", defaultValue = "true") boolean isApprove) {
        if (dto.getTargetVersion() <= 0) {
            long version = configService.getCurrentPublishedData(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile())).getVersion();
            checkArgument(version > Constants.NO_FILE_VERSION, "该文件没有发布过！");
            dto.setTargetVersion((int)version);
        }
        CandidateSnapshot currentSnapshot = configService
                .currentEditSnapshot(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()));
        CandidateSnapshot targetSnapshot = configService
                .getCandidateDetails(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getTargetVersion());

        Preconditions.checkNotNull(currentSnapshot, "file not found");
        checkArgument(dto.getCurrentVersion() > dto.getTargetVersion(), "currentVersion must bigger than targetVersion");
        checkArgument(dto.getCurrentVersion() == currentSnapshot.getEditVersion(), "not the newest version");
        checkArgument(!(isApprove && !targetSnapshot.getStatus().equals(StatusType.PUBLISH)), "not published Version");
        checkArgument(currentSnapshot.getStatus() != StatusType.PASSED, "currentVersion is Passed, can`t rollback");

        CandidateDTO newDto = new CandidateDTO(dto.getGroup(), dto.getDataId(), dto.getProfile(),
                dto.getTargetVersion(), dto.getCurrentVersion(), StatusType.PENDING, targetSnapshot.getData());

        Optional<TemplateInfo> template = templateService.getTemplateInfoByFile(dto.getGroup(), dto.getDataId(), dto.getProfile(),
                dto.getTargetVersion());
        if ((targetSnapshot.getDataId().endsWith(".t") || targetSnapshot.getDataId().endsWith(".json")) && template.isPresent() ) {
            TemplateInfo templateInfo = template.get();
            int version = templateInfo.getVersion();
            if (version > 0) {
                newDto.setTemplateVersion(version);
            }
            newDto.setTemplate(templateInfo.getTemplate());
            newDto.setTemplateGroup(templateInfo.getGroup());
        }

        newDto.setStatus(StatusType.PENDING);
        checkCandidate(newDto);


        logger.info("rollback with {} from {} to {}", newDto, dto.getCurrentVersion(), dto.getTargetVersion());
        Monitor.ROLLBACK_STATICS.inc();

        applyService.rollBack(newDto, isApprove);

        return JsonV2.success();
    }

    /**
     * 批量发布
     *
     * @param actionVos 需要发布的文件list
     * @return 失败列表
     */
    @RequestMapping(value = "/batchPublish", method = RequestMethod.POST)
    @ResponseBody
    public Object batchPublish(@RequestBody List<ActionVo> actionVos) {
        checkArgument(actionVos != null, "list can not be null");
        Monitor.BATCH_PUBLISH_STATICS.inc();

        logger.info("batch publish with {} candidate", actionVos.size());
        List<Map<String, String>> resultList = Lists.newLinkedList();
        for (ActionVo actionVo : actionVos) {
            CandidateDTO candidate = ActionVo.toCandidate(actionVo);
            logger.info("publish with {}", candidate);
            checkCandidate(candidate);
            Monitor.PUBLISH_STATICS.inc();
            try {
                applyService.publish(candidate, "");
            } catch (Exception e) {
                Map<String, String> failMap = processException(candidate, e);
                resultList.add(failMap);
            }
        }
        return JsonV2.successOf(resultList);
    }

    /**
     * 批量审批
     *
     * @param actionVos 需要的发布list
     * @return 失败列表
     */
    @RequestMapping(value = "/batchApprove", method = RequestMethod.POST)
    @ResponseBody
    public Object batchApprove(@RequestBody List<ActionVo> actionVos) {
        checkArgument(actionVos != null, "list can not be null");

        Monitor.BATCH_APPROVE_STATICS.inc();

        logger.info("batch approve with {} candidate", actionVos.size());
        List<Map<String, String>> resultList = Lists.newLinkedList();
        for (ActionVo actionVo : actionVos) {
            CandidateDTO candidate = ActionVo.toCandidate(actionVo);
            logger.info("approve with {}", candidate);
            checkCandidate(candidate);
            Monitor.APPROVE_STATICS.inc();
            try {
                applyService.approve(candidate, "");
            } catch (Exception e) {
                Map<String, String> failMap = processException(candidate, e);
                resultList.add(failMap);
            }
        }
        return JsonV2.successOf(resultList);
    }

    /**
     * 批量拒绝
     *
     * @param actionVos 拒绝列表
     * @return 失败条目
     */
    @RequestMapping(value = "/batchReject", method = RequestMethod.POST)
    @ResponseBody
    public Object batchReject(@RequestBody List<ActionVo> actionVos) {
        checkArgument(actionVos != null, "list can not be null");
        Monitor.BATCH_REJECT_STATICS.inc();

        logger.info("batch Reject with {} candidate", actionVos.size());
        List<Map<String, String>> resultList = Lists.newLinkedList();
        for (ActionVo actionVo : actionVos) {
            CandidateDTO candidate = ActionVo.toCandidate(actionVo);
            logger.info("reject with {}", candidate);
            checkCandidate(candidate);
            Monitor.REJECT_STATICS.inc();
            try {
                applyService.reject(candidate, "");
            } catch (Exception e) {
                Map<String, String> failMap = processException(candidate, e);
                resultList.add(failMap);
            }
        }
        return JsonV2.successOf(resultList);
    }

    private static class PropertyConflict implements Comparable<PropertyConflict> {

        private String key;

        private List<ConfigMeta> metas;

        PropertyConflict(String key, List<ConfigMeta> metas) {
            this.key = key;
            this.metas = metas;
        }

        public String getKey() {
            return key;
        }

        public List<ConfigMeta> getMetas() {
            return metas;
        }

        @Override
        public int compareTo(PropertyConflict o) {
            return this.key.compareTo(o.key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyConflict that = (PropertyConflict) o;

            return Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Conflicts{" +
                    "key='" + key + '\'' +
                    ", metas=" + metas +
                    '}';
        }
    }

}
