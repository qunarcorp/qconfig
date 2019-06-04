package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.cloud.vo.TemplateMetaWithName;
import qunar.tc.qconfig.admin.dao.DefaultTemplateConfigDao;
import qunar.tc.qconfig.admin.dao.DefaultTemplateConfigMappingDao;
import qunar.tc.qconfig.admin.dao.FileTemplateDao;
import qunar.tc.qconfig.admin.dao.FileTemplateMappingDao;
import qunar.tc.qconfig.admin.dao.FileTemplateSnapshotDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.IllegalTemplateException;
import qunar.tc.qconfig.admin.exception.TemplateDealException;
import qunar.tc.qconfig.admin.exception.TemplateNameNotMatchException;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateType;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.template.TemplateCheckerService;
import qunar.tc.qconfig.admin.service.template.TemplateContants;
import qunar.tc.qconfig.admin.service.template.TemplateUtils;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.support.PropertiesUtil;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author zhenyu.nie created on 2016 2016/1/27 14:25
 */
@Service
public class FileTemplateServiceImpl implements FileTemplateService {
    private static final Logger LOG = LoggerFactory.getLogger(FileTemplateServiceImpl.class);

    private static final ObjectMapper MAPPER = TemplateUtils.getMapper();
    private static final int MAX_TEMPLATE_DESC_LENGTH = 150;
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 50;
    private static final int LOWEST_VERSION = 0;
    private static final int ERROR_NUMBER = -1;

    private static final Function<TemplateInfo, String> NAME_EXTRACTOR = new Function<TemplateInfo, String>() {
        @Override
        public String apply(TemplateInfo info) {
            if (info == null) {
                return null;
            }
            return info.getTemplate();
        }
    };

    @Resource
    private UserContextService userContextService;

    @Resource
    private FileTemplateMappingDao templateMappingDao;

    @Resource
    private FileTemplateDao templateDao;

    @Resource
    private FileTemplateSnapshotDao templateSnapshotDao;

    @Resource
    private TemplateCheckerService templateCheckerService;

    @Resource
    private DefaultTemplateConfigDao defaultTemplateConfigDao;

    @Resource
    private DefaultTemplateConfigMappingDao defaultTemplateConfigMappingDao;

    @Override
    public Optional<TemplateInfo> getTemplateInfo(String group, String template) {
        try {
            TemplateInfo templateInfo = templateDao.selectTemplateInfo(group, template);
            dealNewJsonTemplate(templateInfo);
            return Optional.ofNullable(templateInfo);
        } catch (EmptyResultDataAccessException ignore) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TemplateInfo> getTemplateInfoWithoutConvert(String group, String template) {
        try {
            return Optional.ofNullable(templateDao.selectTemplateInfo(group, template));
        } catch (EmptyResultDataAccessException ignore) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getTemplateDetail(String group, String template) {
        TemplateInfo templateInfo = templateDao.selectTemplateInfo(group, template);
        if (templateInfo == null) {
            return Optional.empty();
        } else {
            dealNewJsonTemplate(templateInfo);
        }
        return Optional.ofNullable(templateInfo.getDetail());
    }

    @Override
    public Optional<String> processTemplateValue(CandidateDTO dto) throws IllegalTemplateException {
        final Optional<TemplateInfo> info = getTemplateInfo(dto.getTemplateGroup(), dto.getTemplate());
        if (!info.isPresent()) {
            throw new IllegalTemplateException(dto.getTemplate());
        }

        if (info.get().getType() != TemplateType.TABLE) {
            return Optional.empty();
        }

        int version;
        if (dto.getTemplateVersion() > 0) {
            version = dto.getTemplateVersion();
        } else {
            version = templateMappingDao.selectTemplateVersion(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());
        }

        Optional<String> templateDetail;

        if (version == AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST) {
            templateDetail = getTemplateDetail(dto.getTemplateGroup(), dto.getTemplate());
        } else {
            templateDetail = getTemplateDetail(dto.getTemplateGroup(), dto.getTemplate(), version);
        }
        if (!templateDetail.isPresent()) {
            throw new IllegalTemplateException(dto.getTemplate());
        }
        String data = templateCheckerService.processDetailContent(dto.getData(), templateDetail.get());
        Optional<String> result = templateCheckerService.processValue(data, templateDetail.get());
        if (result.isPresent()) {
            return result;
        } else {
            return Optional.of(data);
        }
    }

    @Override
    public void checkPropertiesTemplateValue(CandidateDTO dto) {

        Map<String, ObjectNode> nameDetailMapping = Maps.newHashMap();
        Map<String, String> nameTypeMapping = Maps.newHashMap();
        parseJsonTemp(dto.getTemplateDetail(), nameTypeMapping, nameDetailMapping);
        if (!Strings.isNullOrEmpty(dto.getInheritGroupId()) && !Strings.isNullOrEmpty(dto.getInheritDataId())
                && !Strings.isNullOrEmpty(dto.getInheritProfile())) {//继承文件模板校验
            ConfigMeta parentMeta = new ConfigMeta(dto.getInheritGroupId(), dto.getInheritDataId(),
                    dto.getInheritProfile());
            Optional<String> parentTempDetail = getPropertiesTemplate(parentMeta);
            Map<String, ObjectNode> parentNameDetailMapping = Maps.newHashMap();
            Map<String, String> parentNameTypeMapping = Maps.newHashMap();
            parentTempDetail.ifPresent(s -> FileTemplateServiceImpl.this.parseJsonTemp(s, parentNameTypeMapping, parentNameDetailMapping));
            templateCheckerService.checkInheritConstraint(dto.getData(), parentNameDetailMapping, parentNameTypeMapping);
            if (parentTempDetail.isPresent() && !parentNameDetailMapping.isEmpty() && !parentNameTypeMapping.isEmpty()) {
                if (!notHasPropertiesTemplate(dto) || !notHasPropertiesTemplate(dto.getInheritDataId(), parentTempDetail.get())) {
                    templateCheckerService.checkInheritableProperties(dto.getData(), nameDetailMapping, nameTypeMapping, parentNameDetailMapping, parentNameTypeMapping);// 父模板优先级高
                }
            } else {
                templateCheckerService.checkProperties(dto.getData(), nameDetailMapping, nameTypeMapping);
            }
        } else {
            if (!notHasPropertiesTemplate(dto)) {
                templateCheckerService.checkProperties(dto.getData(), nameDetailMapping, nameTypeMapping);
            }
        }
    }

    /**
     * 将json格式配置模板转换成map对象
     */
    private void parseJsonTemp(String templateDetail, Map<String, String> nameTypeMapping, Map<String, ObjectNode> nameDetailMapping) {
        if (Strings.isNullOrEmpty(templateDetail)) {
            return;
        }
        try {
            JsonNode detail = MAPPER.readTree(templateDetail);
            Iterator<JsonNode> columnsDetailIterator = detail.elements();
            while (columnsDetailIterator.hasNext()) {
                JsonNode columnDetail = columnsDetailIterator.next();
                nameTypeMapping.put(columnDetail.get("name").asText(), columnDetail.get("type").asText());
                nameDetailMapping.put(columnDetail.get("name").asText(), (ObjectNode) columnDetail);
            }
        } catch (IOException e) {
           LOG.error("", e);
        }
    }

    private boolean notHasPropertiesTemplate(CandidateDTO dto) {
        return notHasPropertiesTemplate(dto.getDataId(), dto.getTemplateDetail());
    }

    private boolean notHasPropertiesTemplate(String datatId, String templateDetail) {
        return !FileChecker.isPropertiesFile(datatId) || StringUtils.isBlank(templateDetail);
    }

    @Override
    public void checkPropertiesTemplate(CandidateDTO dto) {
        if (!FileChecker.isPropertiesFile(dto.getDataId())) {
            return;
        }

        String templateDetail = Strings.nullToEmpty(dto.getTemplateDetail()).trim();
        templateCheckerService.checkPropertiesDetail(templateDetail);
    }

    @Override
    public Optional<String> getPropertiesTemplate(ConfigMeta meta) {
        return getTemplateDetailByFile(meta.getGroup(), meta.getDataId());
    }

    @Override
    @Transactional
    public void setPropertiesTemplate(CandidateDTO dto) {

        if (!FileChecker.isPropertiesFile(dto.getDataId())) {
            return;
        }

        String templateName = PropertiesUtil.getTemplateName(dto.getDataId());
        String newDetail = Strings.nullToEmpty(dto.getTemplateDetail()).trim();
        Map.Entry<String, String> mapping = templateMappingDao.selectTemplate(dto.getGroup(), dto.getDataId());
        if (mapping == null) {
            if (!Strings.isNullOrEmpty(newDetail)) {
                saveTemplateDetail(dto.getGroup(), templateName, TemplateType.PROPERTIES, "", newDetail);
                setTemplate(dto.getGroup(), dto.getDataId(), dto.getGroup(), templateName);
            }
        } else {
            String oldDetail = templateDao.selectTemplateDetail(dto.getGroup(), templateName);
            if (!Objects.equal(oldDetail, newDetail)) {
                saveTemplateDetail(dto.getGroup(), templateName, TemplateType.PROPERTIES, "", newDetail);
            }
        }
    }

    @Override
    public void setDefaultConfigId(ConfigMeta meta, long configId) {
        if (configId > 0) {
            defaultTemplateConfigMappingDao.insert(meta, configId);
        }
    }

    @Override
    public void deleteDefaultConfigId(ConfigMeta meta) {
        defaultTemplateConfigMappingDao.delete(meta);
    }

    @Override
    public long getDefaultConfigId(ConfigMeta meta) {
        Long configId = defaultTemplateConfigMappingDao.select(meta);
        return configId != null ? configId : 0;
    }

    @Override
    public Optional<String> getTemplateDetailWithDefaultConfigId(String group, String template, long defaultConfigId) {
        Optional<String> templateDetail = getTemplateDetail(group, template);
        if (!templateDetail.isPresent()) {
            return Optional.empty();
        }
        return getTemplateDetailWithDefaultConfig(defaultConfigId, templateDetail.get());
    }

    private String dealDefaultConfig(String detail, String defaultConfig) {
        try {
            ObjectNode defaultNode = (ObjectNode) MAPPER.readTree(defaultConfig);
            Iterator<Map.Entry<String, JsonNode>> fields = defaultNode.fields();
            Map<String, String> defaults = Maps.newHashMap();
            Map<String, String> readonly = Maps.newHashMap();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                defaults.put(field.getKey(), field.getValue().get(TemplateContants.DEFAULT).asText());
                readonly.put(field.getKey(), field.getValue().get(TemplateContants.READONLY).asText());
            }

            ObjectNode detailNode = (ObjectNode) MAPPER.readTree(detail);
            JsonNode columns = detailNode.get(Constants.COLUMNS);
            for (JsonNode node : columns) {
                ObjectNode columnNode = (ObjectNode) node;
                String name = columnNode.get(TemplateContants.NAME).asText();
                if (defaults.containsKey(name)) {
                    columnNode.set(TemplateContants.DEFAULT, new TextNode(defaults.get(name)));
                    columnNode.set(TemplateContants.READONLY,
                            BooleanNode.valueOf(Boolean.parseBoolean(readonly.get(name))));
                }
            }
            return MAPPER.writeValueAsString(detailNode);
        } catch (Exception e) {
            LOG.error("parse template with default error, default is [%s], detail is [%s]", defaultConfig, detail, e);
            throw new IllegalStateException("解析模版失败");
        }
    }

    @Override
    public Optional<TemplateInfo> getTemplateInfo(String group, String template, int version) {
        TemplateInfo templateInfo = templateSnapshotDao.selectTemplateInfo(group, template, version);
        dealNewJsonTemplate(templateInfo);
        return Optional.ofNullable(templateInfo);
    }

    private void dealNewJsonTemplate(TemplateInfo templateInfo) {
        if (templateInfo != null && templateInfo.getType().equals(TemplateType.JSON_SCHEMA)) {
            String detail = templateInfo.getDetail();
            String templateDetail = getTemplateDetailFormDetail(detail);
            Optional<String> limitName = getLimitNameFormDetail(detail);
            if (!Strings.isNullOrEmpty(templateDetail) && limitName.isPresent()) {
                templateInfo.setDetail(templateDetail);
            }
        }
    }

    @Override
    public Optional<TemplateInfo> getTemplateInfoWithoutConvert(String group, String template, int version) {
        return Optional.ofNullable(templateSnapshotDao.selectTemplateInfo(group, template, version));
    }

    @Override
    public Optional<String> getTemplateDetail(String group, String template, int version) {
        TemplateInfo templateInfo = templateSnapshotDao.selectTemplateInfo(group, template, version);
        if (templateInfo == null) {
            return Optional.empty();
        } else {
            dealNewJsonTemplate(templateInfo);
        }
        return Optional.ofNullable(templateInfo.getDetail());
    }

    @Override
    public Optional<String> getTemplateDetailWithDefaultConfigId(String group, String template, int version,
                                                                 long defaultConfigId) {
        TemplateInfo templateInfo = templateSnapshotDao.selectTemplateInfo(group, template, version);
        if (templateInfo == null) {
            return Optional.empty();
        } else {
            dealNewJsonTemplate(templateInfo);
        }
        return getTemplateDetailWithDefaultConfig(defaultConfigId, templateInfo.getDetail());
    }

    private Optional<String> getTemplateDetailWithDefaultConfig(long defaultConfigId, String templateDetail) {
        if (templateDetail == null || defaultConfigId == 0) {
            return Optional.ofNullable(templateDetail);
        }

        String defaultConfig = defaultTemplateConfigDao.select(defaultConfigId);
        if (Strings.isNullOrEmpty(defaultConfig)) {
            throw new IllegalArgumentException("无效的config id [" + defaultConfig + "]");
        }

        return Optional.of(dealDefaultConfig(templateDetail, defaultConfig));
    }

    @Override
    public Optional<String> getTemplateDetailByFile(String group, String dataId) {
        Map.Entry<String, String> entry = templateMappingDao.selectTemplate(group, dataId);
        if (entry == null) {
            return Optional.empty();
        }

        String detail = templateDao.selectTemplateDetail(entry.getKey(), entry.getValue());
        return Optional.ofNullable(detail);
    }

    @Override
    @Transactional
    public void saveTemplateDetail(String group, String template, TemplateType type, String description, String detail) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can note be empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "模版名不能为空");
        Preconditions.checkArgument(
                type == TemplateType.PROPERTIES || !template.endsWith(AdminConstants.PROPERTIES_TEMPLATE_SUFFIX),
                "非properties模版名不能以[%s]结尾", AdminConstants.PROPERTIES_TEMPLATE_SUFFIX);
        if (Strings.isNullOrEmpty(detail)) {
            detail = "";
        } else {
            detail = detail.trim();
        }

        checkTemplateName(template);
        checkTemplateDescription(description);
        if (type == TemplateType.TABLE) {
            templateCheckerService.checkDetail(detail);
            TemplateInfo currentInfo = templateDao.selectTemplateInfo(group, template);

            if (currentInfo != null) {
                checkTemplateLimitName(currentInfo.getDetail(), detail);
            }
        } else if (type == TemplateType.JSON_SCHEMA) {
            TemplateInfo currentInfo = templateDao.selectTemplateInfo(group, template);
            if (currentInfo != null) {
                detail = getNewDetail(detail, currentInfo);
            }
        }

        String rtxId = userContextService.getRtxId();
        templateDao.setTemplate(group, template, type, description, detail, rtxId);
        Integer version = templateDao.selectVersion(group, template);
        templateSnapshotDao.insertTemplate(group, template, type, description, detail, rtxId, version);

    }

    /**
     * 这里防止旧前端提交了一个旧版本的模版进行更新
     *
     * @param detail 需要保存的detail
     * @param currentInfo 当前书记info
     * @return 处理好的detail
     */
    // TODO: 2018-12-25 去掉这里
    private String getNewDetail(String detail, TemplateInfo currentInfo) {
        Optional<String> currentName = getLimitNameFormDetail(currentInfo.getDetail());

        if (!getLimitNameFormDetail(detail).isPresent()
                && Strings.isNullOrEmpty(getTemplateDetailFormDetail(detail))
                && currentName.isPresent()
                && !Strings.isNullOrEmpty(getTemplateDetailFormDetail(currentInfo.getDetail()))) {
            Map<String, String> detailMap = Maps.newHashMap();
            detailMap.put("fileName", currentName.get());
            detailMap.put("templateDetail", detail);
            try {
                return new ObjectMapper().writeValueAsString(detailMap);
            } catch (JsonProcessingException e) {
                throw new TemplateDealException();
            }
        }
        return detail;
    }

    private void checkTemplateDescription(String description) {
        Preconditions.checkArgument(description.length() <= MAX_TEMPLATE_DESC_LENGTH, "模版描述不能超过%s个字",
                MAX_TEMPLATE_DESC_LENGTH);
    }

    private void checkTemplateLimitName(String currentDetail, String newDetail) {
        if (!Objects.equal(getLimitNameFormDetail(currentDetail),
                getLimitNameFormDetail(newDetail))) {
            throw new TemplateNameNotMatchException();
        }
    }

    private void checkTemplateName(String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("name[" + name + "] 长度必须在" + MIN_LENGTH + "到" + MAX_LENGTH + "之间");
        }

        for (char c : name.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (isChinese(c)) {
                continue;
            }
            switch (c) {
                case '-':
                case '_':
                case '.':
                    continue;
                default:
                    throw new IllegalArgumentException("name[" + name + "] 只能使用中文，字母和[-_.]");
            }
        }
    }

    private boolean isChinese(char c) {
        return c >= 0x4e00 && c <= 0x9fbb;
    }

    @Override
    public List<TemplateInfo> getTemplateInfoWithoutPropertiesByGroup(String group) {
        return Ordering.natural().onResultOf(NAME_EXTRACTOR)
                .immutableSortedCopy(excludePropertiesTemplate(templateDao.queryTemplateInfoByGroup(group)));
    }

    public List<TemplateInfo> getTemplateInfoListHistory(String group, String template) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can not be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "template can not be null");

        return templateSnapshotDao.selectTemplateInfo(group, template);
    }

    private List<TemplateInfo> excludePropertiesTemplate(List<TemplateInfo> templates) {
        List<TemplateInfo> result = Lists.newArrayList();
        for (TemplateInfo template : templates) {
            if (template.getType() != TemplateType.PROPERTIES) {
                result.add(template);
            }
        }
        return result;
    }

    private List<TemplateInfo> excludePropertiesTemplateMeta(List<TemplateInfo> templates) {
        List<TemplateInfo> result = Lists.newArrayList();
        for (TemplateInfo template : templates) {
            if (template.getType() != TemplateType.PROPERTIES) {
                result.add(template);
            }
        }
        return result;
    }

    @Override
    public Optional<Map.Entry<String, String>> getTemplate(String group, String dataId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can note be empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataId), "dataId can not be empty");

        Map.Entry<String, String> entry = templateMappingDao.selectTemplate(group, dataId);
        return Optional.ofNullable(entry);
    }

    @Override
    public void setTemplate(String group, String dataId, String templateGroup, String template) {
        checkArgument(group, dataId, templateGroup, template);
        template = Strings.nullToEmpty(template);
        if (!Strings.isNullOrEmpty(template)) {
            Preconditions.checkArgument(templateDao.selectTemplateDetail(templateGroup, template) != null, "group=[%s], template=[%s]不存在", templateGroup, template);
        }
        templateMappingDao.setTemplate(group, dataId, templateGroup, template);
    }

    @Override
    public void setTemplate(String group, String dataId, String profile, int dataVersion, String templateGroup,
            String template, int templateVersion) {
        checkArgument(group, dataId, templateGroup, template);
        Preconditions.checkArgument(dataVersion >= 0 && templateVersion >= 0, "version must bigger than 0 or equal 0");

        template = Strings.nullToEmpty(template);
        if (!Strings.isNullOrEmpty(template)) {
            Preconditions.checkArgument(templateDao.selectTemplateDetail(templateGroup, template) != null,
                    "group=[%s], template=[%s]不存在", templateGroup, template);
        }
        templateMappingDao
                .setTemplateWithVersion(group, dataId, profile, templateGroup, template, dataVersion, templateVersion);
    }

    private void checkArgument(String group, String dataId, String templateGroup, String template) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can note be empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataId), "dataId can not be empty");
        Preconditions.checkArgument((Strings.isNullOrEmpty(templateGroup) && Strings.isNullOrEmpty(template)) || (
                        !Strings.isNullOrEmpty(templateGroup) && !Strings.isNullOrEmpty(template)),
                "templateGroup和template要么全为空, 要么全不为空, [%s], [%s]", templateGroup, template);
    }

    @Override
    public Map<String, List<TemplateMetaWithName>> getTemplatesWithDefaultWithoutProperties(Set<String> userGroups) {
        Set<String> groups = Sets.newHashSet(userGroups);
        groups.add(AdminConstants.DEFAULT_TEMPLATE_GROUP);
        Map<String, List<TemplateMetaWithName>> map = Maps.newHashMap();
        List<TemplateInfo> templateMetas = excludePropertiesTemplateMeta(templateDao.queryTemplateInfo(groups));

        List<TemplateMetaWithName> metaWithNames = templateMetas.stream().map(input -> {
            Optional<String> limitNameOptional = getLimitNameFormDetail(input.getDetail());
            String limitName = limitNameOptional.orElse("");
            return new TemplateMetaWithName(input.getGroup(),
                    input.getTemplate(),
                    input.getDescription(),
                    input.getType(), limitName);
        }).collect(Collectors.toList());

        for (TemplateMetaWithName meta : metaWithNames) {
            String group = meta.getGroup();
            //tole
            meta.setGroup(group);
            List<TemplateMetaWithName> templates = map.computeIfAbsent(group, k -> Lists.newArrayList());
            templates.add(meta);
        }
        if (!map.containsKey(AdminConstants.DEFAULT_TEMPLATE_GROUP)) {
            map.put(AdminConstants.DEFAULT_TEMPLATE_GROUP, ImmutableList.of());
        }
        Map<String, List<TemplateMetaWithName>> result = Maps.newTreeMap();
        for (Map.Entry<String, List<TemplateMetaWithName>> entry : map.entrySet()) {
            result.put(entry.getKey(), Ordering.natural().immutableSortedCopy(entry.getValue()));
        }
        return result;
    }

    @Override
    public long saveDefaultTemplateConfig(String defaultConfig) {
        return defaultTemplateConfigDao.insert(defaultConfig);
    }

    private Optional<String> getLimitNameFormDetail(String templateDetail) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(templateDetail);
            JsonNode nameNode = node.get("fileName");
            if (nameNode != null && !nameNode.isNull()) {
                return Optional.of(nameNode.asText());
            }
        } catch (IOException e) {
            LOG.error("校验反序列化失败");
        }
        return Optional.empty();
    }

    /**
     * 从新版本的detail中获取具体的templateDetail
     *
     * @param detail 新版本detail
     * @return templateDetail
     *
     */
    private String getTemplateDetailFormDetail(String detail) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(detail);
            JsonNode nameNode = node.get("templateDetail");
            if (nameNode == null || nameNode.isNull()) {
                return "";
            } else {
                return nameNode.asText();
            }
        } catch (IOException e) {
            LOG.error("校验反序列化失败");
        }
        return "";
    }

    /**
     * 返回当前文件对应的模版版本
     *
     * @param group       模版组
     * @param dataId      appId
     * @param dataVersion 查询的文件版本号
     * @return 当前版本号，如果没有则返回-1
     */
    @Override
    public int getFileMappingTemplateCurrentVersion(String group, String dataId, String profile, long dataVersion) {
        int result = templateMappingDao.selectTemplateVersion(group, dataId, profile, dataVersion);
        if (result == AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST) {
            return templateMappingDao.selectOldTemplateVersion(group, dataId, profile);
        }
        return result;
    }

    /**
     * 添加文件与模版文件的映射
     *
     * @param group           group
     * @param dataId          data_id
     * @param profile         profile
     * @param dataVersion     文件版本
     * @param templateGroup   模版group
     * @param template        模版
     * @param templateVersion 模版版本
     * @return 条数（只有1）
     */
    @Override
    public int setFileTemplateMapping(String group, String dataId, String profile, int dataVersion,
            String templateGroup, String template, int templateVersion) {
        Preconditions.checkArgument(templateVersion > AdminConstants.TEMPLATE_VERSION_NOT_EXIST, "templateVersion必须大于0");
        return templateMappingDao
                .setTemplateWithVersion(group, dataId, profile, templateGroup, template, dataVersion, templateVersion);
    }

    @Override
    public Optional<TemplateInfo> getTemplateInfoByFile(String group, String dateId, String profile, long dataVersion) {
        int templateVersion = getFileMappingTemplateCurrentVersion(group, dateId, profile,dataVersion);

        Map.Entry<String, String> templateInfo = templateMappingDao.selectTemplate(group, dateId);

        if (templateInfo == null) {
            return Optional.empty();
        }

        TemplateInfo info;
        if (templateVersion != AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST) {
            info = templateSnapshotDao
                    .selectTemplateInfo(templateInfo.getKey(), templateInfo.getValue(), templateVersion);
        } else {
            info = templateDao.selectTemplateInfo(templateInfo.getKey(), templateInfo.getValue());
        }

        return Optional.of(info);
    }

    /**
     * 获取这个模版的最新版本号
     * @param group group
     * @param templateId template
     * @return 模版最新版本号
     */
    @Override
    public int getNewestTemplateVersion(String group, String templateId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(templateId), "dataId can not be null or empty");
        return templateDao.selectVersion(group, templateId);
    }

    @Override
    public void completeDelete(ConfigMeta meta) {
        templateMappingDao.completeDelete(meta.getGroup(), meta.getDataId(), meta.getProfile());
    }
}
