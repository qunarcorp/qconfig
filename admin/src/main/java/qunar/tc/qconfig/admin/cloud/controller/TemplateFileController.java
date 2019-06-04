package qunar.tc.qconfig.admin.cloud.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.TemplateMetaWithName;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dto.TemplateVersionDTO;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateType;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.template.TemplateCheckerService;
import qunar.tc.qconfig.admin.service.template.TemplateParserTool;
import qunar.tc.qconfig.admin.service.template.TemplateUtils;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/qconfig/template")
public class TemplateFileController extends AbstractControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(TemplateFileController.class);

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private FileTemplateService fileTemplateService;

    @Resource
    private UserContextService userContextService;

    @Resource
    private TemplateCheckerService templateCheckerService;

    @Resource
    private TemplateParserTool templateParserTool;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object manageTemplate(@RequestParam String group) {
        checkLegalGroup(group);
        return JsonV2.successOf(fileTemplateService.getTemplateInfoWithoutPropertiesByGroup(group));
    }

    @RequestMapping(value = "/view/templateDetailWithData")
    @ResponseBody
    public Object getTemplateDetailWithData(@RequestParam("group") String group,
            @RequestParam("profile") String profile, @RequestParam("dataId") String dataId,
            @RequestParam("version") long version, @RequestParam("template") String template,
            @RequestParam("templateGroup") String templateGroup,
            @RequestParam(value = "defaultConfigId", required = false, defaultValue = "0") long defaultConfigId) {
        final String resultData = getCandidateSnapshotData(group, profile, dataId, version);

        int templateVersion = fileTemplateService.getFileMappingTemplateCurrentVersion(group, dataId, profile, version);

        Optional<String> templateOptional;
        if (templateVersion == AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST) {
            templateOptional = fileTemplateService
                    .getTemplateDetailWithDefaultConfigId(templateGroup, template, defaultConfigId);
        } else {
            templateOptional = fileTemplateService
                    .getTemplateDetailWithDefaultConfigId(templateGroup, template, templateVersion, defaultConfigId);
        }
        //TODO 暂时先返回最新的模板
//        templateOptional = fileTemplateService.getTemplateDetailWithDefaultConfigId(templateGroup, template, defaultConfigId);

        final Optional<String> detail = templateOptional;
        if (!detail.isPresent()) {
            return new JsonV2<>(-1, "group=[" + group + "], template=[" + template + "] + 不存在", null);
        }

        int newestVersion = fileTemplateService.getNewestTemplateVersion(templateGroup, template);

        java.util.Optional<String> optional = TemplateUtils.processTimeLongToStr(dataId, resultData, detail.get());
        final String realData = optional.isPresent() ? optional.get() : resultData;
        return JsonV2.successOf(ImmutableMap.of("data", realData, "templateDetail", detail.get(), "currentVersion", templateVersion,
                "newestVersion", newestVersion));
    }

    @RequestMapping("/view/contentWithTemplate")
    @ResponseBody
    public Object getContentWithTemplate(@RequestParam("group") String group,
            @RequestParam("profile") final String profile, @RequestParam("dataId") final String dataId,
            @RequestParam("version") final long version, @RequestParam("templateGroup") String templateGroup,
            @RequestParam("template") final String template) {
        final String content = getCandidateSnapshotData(group, profile, dataId, version);

        int templateVersion = fileTemplateService.getFileMappingTemplateCurrentVersion(group, dataId, profile, version);

        Optional<String> templateOptional;
        if (templateVersion == AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST) {
            templateOptional = fileTemplateService.getTemplateDetailWithDefaultConfigId(templateGroup, template, 0);
        } else {
            templateOptional = fileTemplateService
                    .getTemplateDetailWithDefaultConfigId(templateGroup, template, templateVersion, 0);
        }

        if (templateOptional.isPresent()) {
            String parsedDetail = templateParserTool
                    .parse(new ConfigMeta(group, dataId, profile), templateOptional.get());

            int newestVersion = fileTemplateService.getNewestTemplateVersion(templateGroup, template);
            return JsonV2.successOf(ImmutableMap.of("content", content, "hasTemplate", true, "template", parsedDetail, "currentVersion",
                    templateVersion, "newestVersion", newestVersion));
        } else {
            return JsonV2.successOf(ImmutableMap.of("content", content, "hasTemplate", false, "template", ""));
        }
    }

    private String getCandidateSnapshotData(final String group, final String profile, final String dataId,
            final long version) {
        if (version == INIT_BASED_VERSION) {
            return "";
        } else {
            return candidateSnapshotDao.find(group, dataId, profile, version).getData();
        }
    }

    //tole 看起来与/info有重复, info更详细，remove it
    @RequestMapping(value = "/view/templateDetail")
    @ResponseBody
    public Object getTemplateDetail(@RequestParam String group, @RequestParam String template,
            @RequestParam(value = "version", defaultValue = "0", required = false) int version) {
        java.util.Optional<String> templateDetail;
        if (version == 0) {
            templateDetail = fileTemplateService.getTemplateDetail(group, template);
        } else {
            templateDetail = fileTemplateService.getTemplateDetail(group, template, version);
        }
        if (!templateDetail.isPresent()) {
            return JsonV2.failOf( "group=[" + group + "], template=[" + template + "] + 不存在");
        } else {
            return JsonV2.successOf(0, "", templateDetail.get());
        }
    }

    @RequestMapping(value = "/info")
    @ResponseBody
    public Object getTemplateInfo(@RequestParam String group, @RequestParam String template,
            @RequestParam(value = "version", defaultValue = "0", required = false) int version) {
        java.util.Optional<TemplateInfo> templateInfo;
        if (version == 0) {
            templateInfo = fileTemplateService.getTemplateInfo(group, template);
        } else {
            templateInfo = fileTemplateService.getTemplateInfo(group, template, version);
        }
        if (!templateInfo.isPresent()) {
            return JsonV2.failOf("group=[" + group + "], template=[" + template + "] + 不存在");
        } else {
            return JsonV2.successOf(0, "", templateInfo.get());
        }
    }

    @RequestMapping(value = "/infoNew")
    @ResponseBody
    public Object getTemplateInfoNew(@RequestParam String group, @RequestParam String template,
                                  @RequestParam(value = "version", defaultValue = "0", required = false) int version) {
        java.util.Optional<TemplateInfo> templateInfo;
        if (version == 0) {
            templateInfo = fileTemplateService.getTemplateInfoWithoutConvert(group, template);
        } else {
            templateInfo = fileTemplateService.getTemplateInfoWithoutConvert(group, template, version);
        }
        if (!templateInfo.isPresent()) {
            return JsonV2.failOf("group=[" + group + "], template=[" + template + "] + 不存在");
        } else {
            return JsonV2.successOf(0, "", templateInfo.get());
        }
    }

    @RequestMapping("/exist")
    @ResponseBody
    public Object isTemplateFileExist(@RequestParam String group, @RequestParam String template) {
        java.util.Optional<TemplateInfo> templateInfo = fileTemplateService.getTemplateInfo(group, template);
        return JsonV2.successOf(templateInfo.isPresent());
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Object saveTemplate(@RequestBody TemplateDto templateDto) {
        logger.info("save template, {}", templateDto);
        try {
            checkLegalGroup(templateDto.getGroup());
            fileTemplateService.saveTemplateDetail(templateDto.getGroup(), templateDto.getTemplate(),
                    TemplateType.fromCode(templateDto.getTemplateType()), templateDto.getTemplateDescription(),
                    templateDto.getTemplateDetail());
            return JsonV2.success();
        } catch (IllegalArgumentException e) {
            return new JsonV2<>(-1, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("save template error, {}", templateDto, e);
            return new JsonV2<>(-1, e.getMessage(), null);
        }
    }

    //tole
    @RequestMapping(value = "/listUserDefined")  // "/view/currentTemplates"
    @ResponseBody
    public Object listUserDefinedTemplates() {
        Set<String> userGroups = userContextService.getAccountGroups();
        Map<String, List<TemplateMetaWithName>> groupTemplatesMap = fileTemplateService
                .getTemplatesWithDefaultWithoutProperties(userGroups);
        groupTemplatesMap.remove(AdminConstants.DEFAULT_TEMPLATE_GROUP);
        return JsonV2.successOf(groupTemplatesMap);
    }

    @RequestMapping(value = "/listSystemDefault")
    @ResponseBody
    public Object listSystemDefaultTemplates() {
        return JsonV2.successOf(fileTemplateService.getTemplatesWithDefaultWithoutProperties(ImmutableSet.<String>of()));
    }

    @RequestMapping(value = "/beforeApplyRender", method = RequestMethod.POST)
    @ResponseBody
    public Object beforeApplyRender(@RequestBody CreateWithDefaultDto dto) {
        logger.info("before apply render, {}", dto);
        if (Strings.isNullOrEmpty(dto.getDefaultConfig())) {
            return JsonV2.success();
        }

        try {
            java.util.Optional<String> template = fileTemplateService
                    .getTemplateDetail(dto.getTemplateGroup(), dto.getTemplate());
            if (!template.isPresent()) {
                return new JsonV2<>(-1, "找不到相应模版", null);
            }

            templateCheckerService.checkDefaultConfig(template.get(), dto.getDefaultConfig());
            long defaultConfigId = fileTemplateService.saveDefaultTemplateConfig(dto.getDefaultConfig());
            return JsonV2.successOf(defaultConfigId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("before apply render error, {}", dto, e);
            return new JsonV2<>(-1, "服务器异常", null);
        }
    }

    /**
     * 模版回滚
     *
     * @param dto 回滚信息
     * @return
     */
    @RequestMapping(value = "/rollback", method = RequestMethod.POST)
    @ResponseBody
    public Object rollbackTemplate(@RequestBody TemplateVersionDTO dto) {
        Preconditions.checkArgument(dto.getCurrentVersion() > 0, "current Version");

        java.util.Optional<TemplateInfo> currentInfo = fileTemplateService.getTemplateInfo(dto.getGroup(), dto.getTemplate(), dto.getCurrentVersion());
        java.util.Optional<TemplateInfo> targetInfo = fileTemplateService.getTemplateInfo(dto.getGroup(), dto.getTemplate(), dto.getTargetVersion());

        if (!targetInfo.isPresent() || !currentInfo.isPresent()) {
            return new JsonV2<>(-1, "rollback or current version not found", null);
        }

        if (fileTemplateService.getNewestTemplateVersion(dto.getGroup(), dto.getTemplate()) != dto.getCurrentVersion()) {
            return new JsonV2<>(-1, "current version is not the newest version", null);
        }
        TemplateInfo targetTemplate = targetInfo.get();
        fileTemplateService
                .saveTemplateDetail(dto.getGroup(), dto.getTemplate(), targetTemplate.getType(), targetTemplate.getDescription(),
                        targetTemplate.getDetail());
        return JsonV2.success();
    }

    /**
     * 回滚differ
     *
     * @param dto 对比信息
     * @return 两个版本的文件内容
     */
    @RequestMapping(value = "applyRollback", method = RequestMethod.POST)
    @ResponseBody
    public Object applyRollback(@RequestBody TemplateVersionDTO dto) {

        Preconditions.checkArgument(dto.getCurrentVersion() > 0, "current Version");

        Preconditions.checkArgument(!Strings.isNullOrEmpty(dto.getGroup()), "group can not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dto.getTemplate()), "template can not be null or empty");

        java.util.Optional<TemplateInfo> currentInfo = fileTemplateService.getTemplateInfo(dto.getGroup(), dto.getTemplate(), dto.getCurrentVersion());
        java.util.Optional<TemplateInfo> targetInfo = fileTemplateService.getTemplateInfo(dto.getGroup(), dto.getTemplate(), dto.getTargetVersion());

        Map<String, String> resultMap = Maps.newHashMapWithExpectedSize(2);
        if (currentInfo.isPresent()) {
            resultMap.put("baseTemplate", currentInfo.get().getDetail());
        } else {
            resultMap.put("baseTemplate", "");
        }
        if (targetInfo.isPresent()) {
            resultMap.put("targetTemplate", targetInfo.get().getDetail());
        } else {
            resultMap.put("targetTemplate", "");
        }

        return JsonV2.successOf(resultMap);
    }

    @RequestMapping(value = "templateVersion", method = RequestMethod.GET)
    @ResponseBody
    public Object template(@RequestParam("group") String group, @RequestParam("template") String template) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "template can not be null or empty");

        return JsonV2.successOf(fileTemplateService.getTemplateInfoListHistory(group, template));
    }

    private static class TemplateDto {
        private String group;
        private String profile;
        private String template;
        private String templateDescription;
        private String templateDetail;
        private int templateType;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getTemplateDescription() {
            return templateDescription;
        }

        public void setTemplateDescription(String templateDescription) {
            this.templateDescription = templateDescription;
        }

        public String getTemplateDetail() {
            return templateDetail;
        }

        public void setTemplateDetail(String templateDetail) {
            this.templateDetail = templateDetail;
        }

        public int getTemplateType() {
            return templateType;
        }

        public void setTemplateType(int templateType) {
            this.templateType = templateType;
        }

        @Override
        public String toString() {
            return "TemplateDto{" + "group='" + group + '\'' + ", profile='" + profile + '\'' + ", template='"
                    + template + '\'' + ", templateDescription='" + templateDescription + '\'' + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CreateWithDefaultDto {
        private String group;
        private String profile;
        private String templateGroup;
        private String template;
        private String defaultConfig;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getProfile() {
            return profile;
        }

        public String getTemplateGroup() {
            return templateGroup;
        }

        public void setTemplateGroup(String templateGroup) {
            this.templateGroup = templateGroup;
        }

        public String getTemplate() {
            return template;
        }

        public String getDefaultConfig() {
            return defaultConfig;
        }

        @Override
        public String toString() {
            return "CreateWithDefaultDto{" + "group='" + group + '\'' + ", profile='" + profile + '\''
                    + ", templateGroup='" + templateGroup + '\'' + ", template='" + template + '\''
                    + ", defaultConfig='" + defaultConfig + '\'' + '}';
        }
    }

}
