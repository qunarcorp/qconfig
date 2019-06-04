package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.ClientLogDao;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.dao.DefaultTemplateConfigMappingDao;
import qunar.tc.qconfig.admin.dao.FileContentMD5Dao;
import qunar.tc.qconfig.admin.dao.FilePublicStatusDao;
import qunar.tc.qconfig.admin.dao.FilePushHistoryDao;
import qunar.tc.qconfig.admin.dao.FileValidateUrlDao;
import qunar.tc.qconfig.admin.dao.PushConfigVersionDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dao.ReferenceLogDao;
import qunar.tc.qconfig.admin.dao.ServerDao;
import qunar.tc.qconfig.admin.dao.SnapshotDao;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.ReferencedNowException;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.service.AdminOperateService;
import qunar.tc.qconfig.admin.service.ApplyQueueService;
import qunar.tc.qconfig.admin.service.FileCommentService;
import qunar.tc.qconfig.admin.service.FileDescriptionService;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.PropertiesEntryLogService;
import qunar.tc.qconfig.admin.service.UserBehaviorService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.template.TemplateUtils;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2017 2017/5/12 17:29
 */
@Service
public class AdminOperateServiceImpl implements AdminOperateService {

    private static final Logger logger = LoggerFactory.getLogger(AdminOperateServiceImpl.class);

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Resource
    private UserContextService userContext;

    @Resource
    private ServerDao serverDao;

    @Resource
    private ConfigDao configDao;

    @Resource
    private SnapshotDao snapshotDao;

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private ReferenceLogDao referenceLogDao;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private CandidateDao candidateDao;

    @Resource
    private ClientLogDao clientLogDao;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private ConfigOpLogDao configOpLogDao;

    @Resource
    private FileValidateUrlDao fileValidateUrlDao;

    @Resource
    private DefaultTemplateConfigMappingDao defaultTemplateConfigMappingDao;

    @Resource
    private NotifyService notifyService;

    @Resource
    private PushConfigVersionDao pushConfigVersionDao;

    @Resource
    private FileTemplateService templateService;

    @Resource
    private ApplyQueueService applyService;

    @Resource
    private FilePublicStatusDao filePublicStatusDao;

    @Resource
    private FileDescriptionService descriptionService;

    @Resource
    private FileCommentService commentService;

    @Resource(name = "inheritConfigDao")
    private InheritConfigDaoImpl inheritConfigDao;

    @Resource
    private ProfileService profileService;

    @Resource
    private UserBehaviorService userBehaviorService;

    @Resource
    private PropertiesEntryLogService propertiesEntryLogService;

    @Resource
    private FilePushHistoryDao filePushHistoryDao;

    @Resource
    private FileContentMD5Dao fileContentMD5Dao;

    @Override
    @Transactional
    public void completeDelete(ConfigMeta meta, DbEnv env) throws ReferencedNowException {
        Reference reference = referenceDao.findByReferenced(meta, RefType.REFERENCE.value());
        if (reference != null) {
            throw new ReferencedNowException(reference);
        }

        reference = referenceDao.findByReferenced(meta, RefType.INHERIT.value());
        if (reference != null) {
            throw new ReferencedNowException(reference);
        }

        configDao.completeDelete(meta);
        snapshotDao.completeDelete(meta);
        candidateDao.completeDelete(meta);
        candidateSnapshotDao.completeDelete(meta);
        referenceDao.completeDelete(meta);

    }

    @Override
    @Transactional
    public void completeDeleteAdminInfo(ConfigMeta meta) {
        fileValidateUrlDao.delete(meta);
        defaultTemplateConfigMappingDao.delete(meta);
        userBehaviorService.deleteFavorites(meta);
        propertiesEntryLogService.deleteEntryLog(meta);
        List<Map.Entry<Long, Long>> entries = pushConfigVersionDao.selectIdAndVersions(meta, Long.MAX_VALUE);
        pushConfigVersionDao.delete(entries);
        filePushHistoryDao.completeDelete(meta);
        fileContentMD5Dao.completeDelete(meta);
        templateService.completeDelete(meta);
        commentService.completeDelete(meta);
        notifyService.notifyAdminDelete(meta);
    }

    @Override
    public void completeDeleteLog(ConfigMeta meta) {
        clientLogDao.delete(meta);
        configOpLogDao.completeDelete(meta);
        referenceLogDao.completeDelete(meta);
        configUsedLogDao.completeDelete(meta);
    }

    @Override
    @Transactional
    public void moveFile(ConfigMeta meta, String toProfile) {
        logger.info("move file to [{}], {}", toProfile, meta);
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("group", meta.getGroup());
        parameter.addValue("profile", meta.getProfile());
        parameter.addValue("dataId", meta.getDataId());
        parameter.addValue("toProfile", toProfile);

        namedParameterJdbcTemplate.update("UPDATE config SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_snapshot SET profile=:toProfile WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_candidate SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_candidate_snapshot SET profile=:toProfile WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_log SET profile=:toProfile WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_op_log SET profile=:toProfile,operation_time=operation_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_used_log SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_used_log SET source_profile=:toProfile,update_time=update_time WHERE source_group_id=:group AND source_data_id=:dataId AND source_profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE file_validate_url SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE default_template_config_mapping SET profile=:toProfile WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE properties_entries SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE fixed_consumer_version SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE properties_template SET profile=:toProfile,update_time=update_time WHERE group_id=:group AND data_id=:dataId AND profile=:profile", parameter);

        namedParameterJdbcTemplate.update("UPDATE config_reference SET profile=:toProfile WHERE group_id=:group AND alias=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_reference_log SET profile=:toProfile WHERE group_id=:group AND alias=:dataId AND profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_reference SET ref_profile=:toProfile WHERE ref_group_id=:group AND ref_data_id=:dataId AND ref_profile=:profile", parameter);
        namedParameterJdbcTemplate.update("UPDATE config_reference_log SET ref_profile=:toProfile WHERE ref_group_id=:group AND ref_data_id=:dataId AND ref_profile=:profile", parameter);

        notifyService.notifyAdminDelete(new ConfigMeta(meta.getGroup(), meta.getDataId(), meta.getProfile()));
    }

    @Override
    @Transactional
    public void copyGroup(String fromGroup, String toGroup) {
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("fromGroup", fromGroup);
        parameter.addValue("toGroup", toGroup);

        List<String> profiles = profileService.find(fromGroup);
        for (String profile : profiles) {
            profileService.create(toGroup, profile);
        }

        List<TemplateInfo> templates = templateService.getTemplateInfoWithoutPropertiesByGroup(fromGroup);
        for (TemplateInfo template : templates) {
            templateService.saveTemplateDetail(toGroup, template.getTemplate(), template.getType(), template.getDescription(), template.getDetail());
        }

        List<Config> configs = namedParameterJdbcTemplate.query("SELECT group_id,data_id,profile,version FROM config WHERE group_id=:fromGroup AND public_status!=2", parameter, CONFIG_MAPPER);
        List<Config> toGroupConfigs = namedParameterJdbcTemplate.query("SELECT group_id,data_id,profile,version FROM config WHERE group_id=:toGroup AND public_status!=2", parameter, CONFIG_MAPPER);
        Set<ConfigMeta> toGroupMetas = Sets.newHashSetWithExpectedSize(toGroupConfigs.size());
        for (Config toGroupConfig : toGroupConfigs) {
            toGroupMetas.add(toMetaWithGroup(toGroupConfig, toGroup));
        }
        for (Config config : configs) {
            if (toGroupMetas.contains(toMetaWithGroup(config, toGroup))) {
                continue;
            }
            logger.info("admin copy try publish, {}", config);
            ConfigMeta meta = new ConfigMeta(config.group, config.dataId, config.profile);
            ChecksumData<String> checksumData = snapshotDao.find(VersionData.of(config.version, meta));

            Optional<Map.Entry<String, String>> template = templateService.getTemplate(config.group, config.dataId);
            String templateGroup = "";
            String templateName = "";
            String templateDetail = "";
            String data = checksumData.getData();
            if (template.isPresent()) {
                if (!FileChecker.isPropertiesFile(meta.getDataId())) {
                    String realTemplateGroup = template.get().getKey();
                    String realTemplateName = template.get().getValue();
                    if (realTemplateGroup.equals(fromGroup)) {
                        templateGroup = toGroup;
                    } else {
                        templateGroup = realTemplateGroup;
                    }
                    templateName = realTemplateName;
                    Optional<String> optionalData = TemplateUtils.processTimeLongToStr(meta.getDataId(), data, templateService.getTemplateDetail(realTemplateGroup, realTemplateName).get());
                    if (optionalData.isPresent()) {
                        data = optionalData.get();
                    }
                } else {
                    Optional<String> propertiesTemplate = templateService.getPropertiesTemplate(meta);
                    if (propertiesTemplate.isPresent()) {
                        templateDetail = propertiesTemplate.get();
                    }
                }
            }

            CandidateDTO dto = new CandidateDTO();
            dto.setGroup(toGroup);
            dto.setDataId(config.dataId);
            dto.setProfile(config.profile);
            dto.setDescription(descriptionService.getDescription(config.group, config.dataId));
            dto.setTemplateGroup(templateGroup);
            dto.setTemplate(templateName);
            dto.setTemplateDetail(templateDetail);
            if (FileChecker.isPropertiesFile(meta.getDataId())) {
                ConfigMeta inherit = inheritConfigDao.findReference(meta, RefType.INHERIT.value());
                if (inherit != null) {
                    dto.setInheritGroupId(inherit.getGroup());
                    dto.setInheritDataId(inherit.getDataId());
                    dto.setInheritProfile(inherit.getProfile());
                }
            }
            dto.setData(data);
            dto.setValidateUrl(fileValidateUrlDao.select(meta));
            logger.info("admin one button publish, {}", dto);
            applyService.oneButtonPublish(dto, false);
        }

        List<Reference> references = referenceDao.findReferenceInfos(fromGroup);
        List<Reference> toGroupReferences = referenceDao.findReferenceInfos(toGroup);
        Set<ConfigMeta> toGroupReferenceMetas = Sets.newHashSetWithExpectedSize(toGroupReferences.size());
        for (Reference toGroupReference : toGroupReferences) {
            toGroupReferenceMetas.add(toMetaWithGroup(toGroupReference, toGroup));
        }
        for (Reference reference : references) {
            if (toGroupReferenceMetas.contains(toMetaWithGroup(reference, toGroup))) {
                continue;
            }
            reference.setGroup(toGroup);
            referenceDao.create(reference);
            logger.info("admin reference create, {}", reference);
        }

    }

    @Override
    @Transactional
    public void referenceGroup(String fromGroup, String toGroup) {
        List<String> profiles = profileService.find(fromGroup);
        for (String profile : profiles) {
            profileService.create(toGroup, profile);
        }

        List<Reference> toGroupReferences = referenceDao.findReferenceInfos(toGroup);
        Set<ConfigMeta> toGroupReferenceMetas = Sets.newHashSetWithExpectedSize(toGroupReferences.size());
        for (Reference toGroupReference : toGroupReferences) {
            toGroupReferenceMetas.add(toMetaWithGroup(toGroupReference, toGroup));
        }

        List<Reference> references = referenceDao.findReferenceInfos(fromGroup);
        for (Reference reference : references) {
            if (toGroupReferenceMetas.contains(toMetaWithGroup(reference, toGroup))) {
                continue;
            }
            reference.setGroup(toGroup);
            referenceDao.create(reference);
            logger.info("admin reference create, {}", reference);
        }

        List<String> publics = filePublicStatusDao.selectDataIds(fromGroup, PublicType.PUBLIC_MASK);
        List<String> inherits = filePublicStatusDao.selectDataIds(fromGroup, PublicType.INHERIT_MASK);
        Set<String> needReference = Sets.newHashSet(publics);
        needReference.removeAll(inherits);

        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("fromGroup", fromGroup);
        parameter.addValue("toGroup", toGroup);
        List<Config> configs = namedParameterJdbcTemplate.query("SELECT group_id,data_id,profile,version FROM config WHERE group_id=:fromGroup AND public_status=0", parameter, CONFIG_MAPPER);
        for (Config config : configs) {
            if (toGroupReferenceMetas.contains(toMetaWithGroup(config, toGroup))) {
                continue;
            }
            if (needReference.contains(config.dataId)) {
                Reference reference = new Reference(toGroup, config.profile, config.dataId, fromGroup, config.profile, config.dataId, userContext.getRtxId(), new Timestamp(System.currentTimeMillis()));
                referenceDao.create(reference);
                logger.info("admin reference create, {}", reference);
            }
        }
    }

    @Override
    public int deleteReference(ConfigMeta meta, DbEnv env) {
        return referenceDao.completeDelete(meta);
    }

    @Override
    public int deleteServer(String ip) {
        return serverDao.deleteServer(ip);

    }

    private static class Config {
        private String group;
        private String dataId;
        private String profile;
        private int version;

        public Config(String group, String dataId, String profile, int version) {
            this.group = group;
            this.dataId = dataId;
            this.profile = profile;
            this.version = version;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "group='" + group + '\'' +
                    ", dataId='" + dataId + '\'' +
                    ", profile='" + profile + '\'' +
                    ", version=" + version +
                    '}';
        }
    }

    private static ConfigMeta toMetaWithGroup(Config config, String group) {
        return new ConfigMeta(group, config.dataId, config.profile);
    }

    private static ConfigMeta toMetaWithGroup(Reference reference, String group) {
        return new ConfigMeta(group, reference.getAlias(), reference.getProfile());
    }

    private static final RowMapper<Config> CONFIG_MAPPER = new RowMapper<Config>() {
        @Override
        public Config mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Config(rs.getString("group_id"),
                    rs.getString("data_id"),
                    rs.getString("profile"),
                    rs.getInt("version"));
        }
    };
}
