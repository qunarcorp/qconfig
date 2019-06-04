package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.FileDeleteDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.FileDeletingException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.exception.OnePersonPublishException;
import qunar.tc.qconfig.admin.exception.StatusMismatchException;
import qunar.tc.qconfig.admin.exception.TemplateChangedException;
import qunar.tc.qconfig.admin.exception.TemplateNotExistException;
import qunar.tc.qconfig.admin.exception.ValidateErrorException;
import qunar.tc.qconfig.admin.exception.ValidateMessageException;
import qunar.tc.qconfig.admin.greyrelease.ModificationDuringPublishingException;
import qunar.tc.qconfig.admin.model.*;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.*;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.support.UUIDUtil;
import qunar.tc.qconfig.admin.web.bean.ConfigDetail;
import qunar.tc.qconfig.admin.web.bean.ConfigField;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.client.validate.MapperHolder;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.*;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.ReferenceStatus;
import qunar.tc.qconfig.servercommon.util.PriorityUtil;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 19:53
 */
@Service
public class ApplyQueueServiceImpl implements ApplyQueueService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper mapper = MapperHolder.getObjectMapper();

    private static final long INIT_BASED_VERSION = 0;

    private static final long FORCE_UPDATE_VERSION = -2;

    private static final long CREATE_NEWFILE_VERSION = -1;


    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private UserContextService userContext;

    @Resource
    private FileContentMD5Service fileContentMD5Service;

    @Resource
    private ConfigService configService;

    @Resource
    private CandidateDao candidateDao;

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private CheckEnvConflictService checkEnvConflictService;

    @Resource
    private FileDescriptionService fileDescriptionService;

    @Resource
    private FileCommentService fileCommentService;

    @Resource
    private FileTemplateService fileTemplateService;

    @Resource
    private FilePublicStatusService filePublicStatusService;

    @Resource
    private PropertiesCheckService propertiesCheckService;

    @Resource
    private FileValidateUrlService fileValidateUrlService;

    @Resource
    private FileDeleteDao fileDeleteDao;

    @Resource
    private NotifyService notifyService;

    @Resource(name = "inheritConfigDao")
    private InheritConfigDaoImpl inheritConfigDao;

    @Resource
    private GreyReleaseService greyReleaseService;

    @Resource
    private AdminSelfConfigService adminSelfConfigService;

    @QConfig("config.properties")
    private Map<String, String> qconfigs;

    @Resource
    private ProfileService profileService;

    public UserContextService getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContextService userContext) {
        this.userContext = userContext;
    }

    private Candidate forcePublish(CandidateDTO dto) {
        if (isTemplateFile(dto)) {
            ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
            fileValidateUrlService.setUrl(meta, dto.getValidateUrl());
            fileTemplateService.setDefaultConfigId(meta, dto.getDefaultConfigId());
        }

        CandidateSnapshot oldSnapshot = candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());
        if (oldSnapshot == null || oldSnapshot.getStatus() != StatusType.DELETE) {
            throw new StatusMismatchException();
        }

        correctData(dto, oldSnapshot);

        return updateCandidate(oldSnapshot, StatusType.PUBLISH);
    }

    private Candidate genNewOneButtonPublishCandidate(CandidateDTO dto) {
        Candidate candidate = new Candidate(dto.getGroup(), dto.getDataId(), dto.getProfile());
        candidate.setStatus(StatusType.PUBLISH);
        return candidate;
    }

    private Candidate genUpdateOneButtonPublishCandidate(CandidateDTO dto) {
        CandidateSnapshot oldSnapshot = candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());
        if (oldSnapshot == null || (oldSnapshot.getStatus() != StatusType.PUBLISH
                && oldSnapshot.getStatus() != StatusType.PENDING)) {
            throw new StatusMismatchException();
        }
        Candidate candidate = new Candidate(dto.getGroup(), dto.getDataId(), dto.getProfile());
        dto.setStatus(oldSnapshot.getStatus());//这个值最好前端传过来，目前是没有传递的;这样对于candidatesnapshot的查询可以省去

        candidate.setStatus(StatusType.PUBLISH);
        long basedVersion = dto.getStatus() == StatusType.PUBLISH ? dto.getEditVersion() : dto.getBasedVersion();
        candidate.setBasedVersion(basedVersion);
        candidate.setEditVersion(dto.getEditVersion() + 1);
        return candidate;
    }

    private void createNewConfigPreProcess(CandidateDTO dto, Optional<Map.Entry<String, String>> originTemplate) {
        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        newConfigApplyCheck(meta);
        newConfigApplyRelationInfoProcess(dto, originTemplate);
    }

    @Override
    @Transactional
    public List<InterData> oneButtonBatchPublish(List<CandidateDTO> candidateDTOList) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            long sts = System.currentTimeMillis();
            List<InterData> interDataList = Lists.newLinkedList();
            List<Candidate> candidateBatchSaveList = Lists.newLinkedList();
            List<Candidate> candidateBatchUpdateList = Lists.newLinkedList();
            List<CandidateDTO> dtoBatchProcessList = Lists.newLinkedList();
            List<CandidateSnapshot> snapshotBatchProcessList = Lists.newLinkedList();
            List<String> lockedUUidList = Lists.newLinkedList();
            for (CandidateDTO dto : candidateDTOList) {
                Optional<Map.Entry<String, String>> originTemplate = fileTemplateService.getTemplate(dto.getGroup(), dto.getDataId());
                String theData = applyPreProcess(dto, originTemplate);
                Candidate candidate;
                ApplyResult applyResult;
                if (dto.isForceUpload()) {
                    candidate = forcePublish(dto);
                    applyResult = ApplyResult.UPDATE;
                } else {
                    applyResult = ApplyResult.of(dto.getEditVersion());
                    if (applyResult == ApplyResult.NEW) {
                        createNewConfigPreProcess(dto, originTemplate);
                        candidate = genNewOneButtonPublishCandidate(dto);
                        candidateBatchSaveList.add(candidate);
                    } else {
                        candidate = genUpdateOneButtonPublishCandidate(dto);
                        candidateBatchUpdateList.add(candidate);
                    }
                }

                dto.setEditVersion(candidate.getEditVersion());
                String uuid = UUIDUtil.generate();
                dto.setUuid(uuid);
                lockedUUidList.add(uuid);
                dtoBatchProcessList.add(dto);
                CandidateSnapshot snapshot = new CandidateSnapshot(candidate, theData, userContext.getRtxId());

                fileTemplateService.setPropertiesTemplate(dto);
                if (applyResult != ApplyResult.NEW) {
                    updateCandidateMapping(dto, candidate, true);
                }
                checkValidateFile(snapshot);
                snapshotBatchProcessList.add(snapshot);
                InterData interData = new InterData(dto, applyResult, dto, dto);
                interDataList.add(interData);
            }
            candidateDao.batchUpdate(candidateBatchUpdateList);
            candidateDao.batchSave(candidateBatchSaveList);
            candidateSnapshotDao.batchSave(snapshotBatchProcessList);
            fileDescriptionService.batchSetDescription(dtoBatchProcessList, true);
            lock(dtoBatchProcessList);
            configService.batchPublish(snapshotBatchProcessList);
            unlock(lockedUUidList);
            logger.info("new onebutton puiblish, cost:{}", System.currentTimeMillis() - sts);
            return interDataList;
        } finally {
            Monitor.NEW_PORTAL_BATCH_ONEBUTTON_TIMER.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 处理来自API的请求
     */
    @Override
    @Transactional(rollbackFor = {Throwable.class}, propagation = Propagation.REQUIRES_NEW, timeout = 200)
    public List<InterData> internalBatchSave(List<ConfigDetail> configDetails, boolean isPublic, boolean withPriority) throws FileNotFoundException, JsonProcessingException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<InterData> result = Lists.newLinkedList();
        try {
            for (ConfigDetail configDetail : configDetails) {
                long sts = System.currentTimeMillis();
                ConfigField configField = configDetail.getConfigField();
                if (!checkNotNull(configDetail) || configField == null) {
                    continue;
                }
                CandidateDTO candidateDTO = new CandidateDTO(configField.getGroup()
                        , configField.getDataId()
                        , configField.getProfile()
                        , configDetail.getContent());
                ConfigInfoWithoutPublicStatus currentConfig = findCurrentConfig(configField, withPriority);
                long editVersion = INIT_BASED_VERSION;
                if (configDetail.getVersion() > INIT_BASED_VERSION) {//更新配置
                    checkConfigExists(configDetail, currentConfig);
                    editVersion = configDetail.getVersion();
                } else if (configDetail.getVersion() == CREATE_NEWFILE_VERSION) {//新建配置
                    checkSubEnv(configDetail);
                } else if (configDetail.getVersion() == FORCE_UPDATE_VERSION && currentConfig != null) {//强制更新配置
                    editVersion = currentConfig.getVersion();
                } else {
                    throw new IllegalArgumentException("请传入正确的版本号，-1表示新建，-2表示强制更新，>0表示更新");
                }
                candidateDTO.setEditVersion(editVersion);
                if (currentConfig != null && !Strings.isNullOrEmpty(currentConfig.getProfile())) {
                    candidateDTO.setProfile(currentConfig.getProfile());
                }

                long ets = System.currentTimeMillis();
                logger.info("end pre save process, cost {}: {}", (ets - sts), configDetail);//TODO
                InterData interData = oneButtonPublish(candidateDTO, needForceApply(candidateDTO));
                logger.info("end oneButtonPublish, cost {}", System.currentTimeMillis() - ets);
                result.add(interData);

                if (isPublic && candidateDTO.getEditVersion() == INIT_BASED_VERSION) {
                    makePublic(new ConfigMeta(configField.getGroup(), configField.getDataId(), configField.getProfile()));
                }
            }
        } finally {
            Monitor.batchSaveTimerWithoutPostEvent.update(stopwatch.elapsed().toMillis(), TimeUnit.MICROSECONDS);
        }
        return result;
    }

    @Override
    @Transactional(timeout = 30)
    public InterData oneButtonPublish(CandidateDTO dto, boolean isForceApply) {
        List<CandidateDTO> dtoList = Lists.newLinkedList();
        dtoList.add(dto);
        return oneButtonBatchPublish(dtoList).get(0);
    }

    @Override
    @Transactional
    public boolean approveAndPublish(ConfigMeta configMeta) {
        CandidateDTO approveDto = getCurrentDto(configMeta);
        approve(approveDto);
        CandidateDTO publishDto = getCurrentDto(configMeta);
        publish(publishDto);
        return true;
    }

    private CandidateDTO getCurrentDto(ConfigMeta meta) {
        Candidate candidate = configService.currentEdit(meta);

        CandidateDTO dto = new CandidateDTO();
        dto.setGroup(candidate.getGroup());
        dto.setDataId(candidate.getDataId());
        dto.setProfile(candidate.getProfile());
        dto.setBasedVersion(candidate.getBasedVersion());
        dto.setEditVersion(candidate.getEditVersion());
        dto.setStatus(candidate.getStatus());
        return dto;
    }

    private String templateProcess(CandidateDTO dto, Optional<Map.Entry<String, String>> originTemplate) {
        if (isTemplateFile(dto)) {
            if (originTemplate.isPresent()) {
                Map.Entry<String, String> templateEntry = Maps
                        .immutableEntry(dto.getTemplateGroup(), dto.getTemplate());
                checkTemplateChanged(originTemplate.get(), templateEntry);
            }

            Optional<String> newData = fileTemplateService.processTemplateValue(dto);
            if (newData.isPresent()) {
                return newData.get();
            }
        }
        return dto.getData();
    }

    private String applyPreProcess(CandidateDTO dto, Optional<Map.Entry<String, String>> originTemplate) {
        String theData = templateProcess(dto, originTemplate);
        propertiesCheckService.checkConflictProperty(dto.getGroup(), dto.getDataId(), theData);
        fileTemplateService.checkPropertiesTemplate(dto);
        fileTemplateService.checkPropertiesTemplateValue(dto);
        return theData;
    }

    private void newConfigApplyCheck(ConfigMeta meta) {
        /**
         * 检查新建的配置文件是否已经存在，如果已经存在则存在冲突
         * 有两种冲突的方式：
         * 1. 配置文件已存在，且为其它文件的引用
         * 2. 配置文件已存在，且为本环境下的实体文件
         */
        Optional<Conflict> conflictResult = checkEnvConflictService.getConflict(meta);
        if (conflictResult.isPresent()) {
            throw new ConfigExistException(conflictResult.get());
        }

        if (fileDeleteDao.exist(meta)) {//是否文件正在被删除
            throw new FileDeletingException();
        }
    }

    private void newConfigApplyRelationInfoProcess(CandidateDTO dto, Optional<Map.Entry<String, String>> originTemplate) {
        boolean isTemplateFile = isTemplateFile(dto);
        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        if (isTemplateFile && !Strings.isNullOrEmpty(dto.getValidateUrl())) {
            fileValidateUrlService.setUrl(meta, dto.getValidateUrl());
        }

        if (isTemplateFile && !originTemplate.isPresent()) {
            Optional<TemplateInfo> templateInfo = fileTemplateService.getTemplateInfo(dto.getTemplateGroup(), dto.getTemplate());
            if (templateInfo.isPresent()) {
                fileTemplateService.setTemplate(dto.getGroup(), dto.getDataId(), dto.getProfile(), 1, dto.getTemplateGroup(),
                        dto.getTemplate(), templateInfo.get().getVersion());
            } else {
                throw new TemplateNotExistException();
            }
        }

        if (!Strings.isNullOrEmpty(dto.getInheritGroupId())
                && !Strings.isNullOrEmpty(dto.getInheritDataId())) {
            inheritConfigDao.save(dto, userContext.getRtxId());
        }

        if (isTemplateFile) {
            fileTemplateService.setDefaultConfigId(meta, dto.getDefaultConfigId());
        }
    }

    @Override
    @Transactional
    public ApplyResult apply(CandidateDTO dto) {
        ApplyResult applyResult = ApplyResult.of(dto.getEditVersion());

        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        Optional<Map.Entry<String, String>> originTemplate = fileTemplateService.getTemplate(dto.getGroup(), dto.getDataId());
        String theData = applyPreProcess(dto, originTemplate);

        Candidate candidate;
        if (applyResult == ApplyResult.NEW) {
            candidate = new Candidate(dto.getGroup(), dto.getDataId(), dto.getProfile());
            newConfigApplyCheck(meta);
            newConfigApplyRelationInfoProcess(dto, originTemplate);

            try {
                candidateDao.save(candidate);
            } catch (DuplicateKeyException e) {
                throw new ModifiedException();
            }
        } else {
            CandidateSnapshot oldSnapshot = candidateSnapshotDao.find(
                    dto.getGroup(),
                    dto.getDataId(),
                    dto.getProfile(),
                    dto.getEditVersion());
            if (oldSnapshot.getStatus() == StatusType.PASSED) {
                throw new StatusMismatchException();
            }

            correctData(dto, oldSnapshot);
            candidate = updateCandidate(oldSnapshot, StatusType.PENDING);
        }

        CandidateSnapshot snapshot = new CandidateSnapshot(candidate, theData, userContext.getRtxId());
        checkValidateFile(snapshot);
        candidateSnapshotDao.save(snapshot);
        //这里只能在这里处理
        if (applyResult != ApplyResult.NEW) {
            updateCandidateMapping(dto, candidate, true);
        }

        fileContentMD5Service.applyConfigChange(snapshot);
        fileTemplateService.setPropertiesTemplate(dto);
        updateConfigComment(meta, snapshot.getEditVersion(), dto.getComment());
        return applyResult;
    }

    private boolean isTemplateFile(CandidateDTO dto) {
        return !Strings.isNullOrEmpty(dto.getTemplateGroup()) && !Strings.isNullOrEmpty(dto.getTemplate()) && !FileChecker.isPropertiesFile(dto.getDataId());
    }

    private void checkTemplateChanged(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) throws TemplateChangedException {
        if (!lhs.equals(rhs)) {
            throw new TemplateChangedException();
        }
    }

    @Override
    @Transactional
    public void forceApply(CandidateDTO dto) {
        boolean isTemplateFile = isTemplateFile(dto);
        Optional<Map.Entry<String, String>> originTemplate = fileTemplateService.getTemplate(dto.getGroup(), dto.getDataId());
        String theData = applyPreProcess(dto, originTemplate);

        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        if (isTemplateFile) {
            fileValidateUrlService.setUrl(meta, dto.getValidateUrl());
            fileTemplateService.setDefaultConfigId(meta, dto.getDefaultConfigId());
        }
        CandidateSnapshot oldSnapshot = candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(),
                dto.getProfile(), dto.getEditVersion());
        if (oldSnapshot.getStatus() != StatusType.DELETE) {
            throw new StatusMismatchException();
        }

        correctData(dto, oldSnapshot);
        Candidate candidate = updateCandidate(oldSnapshot, StatusType.PENDING);
        CandidateSnapshot snapshot = new CandidateSnapshot(candidate, theData, userContext.getRtxId());
        checkValidateFile(snapshot);
        updateConfigComment(meta, snapshot.getEditVersion(), dto.getComment());
        candidateSnapshotDao.save(snapshot);
        fileContentMD5Service.applyConfigChange(snapshot);
        fileTemplateService.setPropertiesTemplate(dto);
        updateCandidateMapping(dto, candidate, true);
    }

    private void updateCandidateMapping(CandidateDTO dto, Candidate candidate, boolean isApply) {
        //dto里只有apply/forceApply时才有template/templateGroup字段。审批/发布/删除等操作没有该字段
        if (!dto.getDataId().endsWith(".t") && !dto.getDataId().endsWith(".json")) {
            return;
        }
        Optional<Map.Entry<String, String>> templateInfo = fileTemplateService
                .getTemplate(dto.getGroup(), dto.getDataId());
        if (!templateInfo.isPresent()) {
            return;
        }
        String templateGroup = templateInfo.get().getKey();
        String template = templateInfo.get().getValue();
        int templateVersion = fileTemplateService
                .getFileMappingTemplateCurrentVersion(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());

        if (dto.getTemplateVersion() > 0 && isApply) {
            templateVersion = dto.getTemplateVersion();
        }

        if (templateVersion == AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST) {
            Optional<TemplateInfo> templateInfoOptional = fileTemplateService.getTemplateInfo(templateGroup, template);
            templateVersion = templateInfoOptional.get().getVersion();
        }

        int mappingResult = fileTemplateService
                .setFileTemplateMapping(dto.getGroup(), dto.getDataId(), dto.getProfile(),
                        (int) candidate.getEditVersion(), templateGroup, template, templateVersion);
        if (mappingResult <= 0) {
            throw new ModifiedException();
        }
    }

    private void updateConfigComment(ConfigMeta meta, long version, String comment) {
        if (!Strings.isNullOrEmpty(comment)) {
            fileCommentService.setComment(meta, version, comment);
        }
    }

    private Candidate updateCandidate(CandidateSnapshot oldSnapshot, StatusType resultType) throws ModifiedException {
        long basedVersion = oldSnapshot.getStatus() == StatusType.PUBLISH ? oldSnapshot.getEditVersion() : oldSnapshot
                .getBasedVersion();
        Candidate candidate = new Candidate(oldSnapshot.getGroup(), oldSnapshot.getDataId(), oldSnapshot.getProfile(),
                basedVersion, oldSnapshot.getEditVersion() + 1, resultType);

        int update = candidateDao.update(candidate, oldSnapshot.getStatus());
        if (update == 0) {
            throw new ModifiedException();
        }

        return candidate;
    }

    @Override
    @Transactional
    public void reject(CandidateDTO dto) {
        changeStatusAndSave(dto, StatusType.PENDING, StatusType.REJECT);
    }

    private CandidateSnapshot changeStatusAndSave(CandidateDTO dto,
                                                  StatusType expectType,
                                                  StatusType resultType)
            throws ModifiedException, StatusMismatchException, OnePersonPublishException {
        CandidateSnapshot oldSnapshot = candidateSnapshotDao.find(dto.getGroup(),
                dto.getDataId(),
                dto.getProfile(),
                dto.getEditVersion());
        if (oldSnapshot.getStatus() != expectType) {
            throw new StatusMismatchException();
        }

        if (resultType == StatusType.PASSED || resultType == StatusType.PUBLISH) {
            checkValidateFile(oldSnapshot);
        }

        correctData(dto, oldSnapshot);
        Candidate candidate = updateCandidate(oldSnapshot, resultType);

        CandidateSnapshot snapshot = new CandidateSnapshot(candidate,
                oldSnapshot.getData(),
                userContext.getRtxId());

        candidateSnapshotDao.save(snapshot);
        updateCandidateMapping(dto, candidate, false);
        fileContentMD5Service.applyConfigChange(snapshot);
        updateConfigComment(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()), snapshot.getEditVersion(), dto.getComment());
        return snapshot;
    }

    private void checkValidateFile(CandidateSnapshot snapshot) {
        if (FileChecker.isTemplateFile(snapshot.getDataId())) {
            String validateUrl = fileValidateUrlService.getUrl(
                    new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile()));
            if (!Strings.isNullOrEmpty(validateUrl)) {
                doCheckValidateFile(validateUrl, snapshot);
            }
        }
    }

    private final AsyncHttpClient httpClient = getHttpClient();

    private static final int DEFAULTTIMEOUT = 5000;

    private volatile int REQUESTTIMEOUTMS = 5000;

    private static final int CONNECTTIMEOUT = 2000;

    private static final int REQUESTTIMEOUT = 3000;

    private AsyncHttpClient getHttpClient() {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(conf -> {
            REQUESTTIMEOUTMS = Numbers.toInt(conf.get("qtable.validate.timeoutMs"), DEFAULTTIMEOUT);
            logger.info("qtable validate timeout ms is [{}]", REQUESTTIMEOUTMS);
        });

        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setConnectTimeout(CONNECTTIMEOUT);
        builder.setRequestTimeout(REQUESTTIMEOUT);
        return new AsyncHttpClient(builder.build());
    }

    private void doCheckValidateFile(String validateUrl, CandidateSnapshot snapshot) {
        AsyncHttpClient.BoundRequestBuilder builder = httpClient.preparePost(validateUrl);
        builder.setHeader("content-type", "application/json; charset=utf-8");
        try {
            builder.setBody(mapper.writeValueAsString(new VersionedJsonRequest<>(snapshot.getData(), 0)));
        } catch (JsonProcessingException e) {
            logger.error("write qtable data to json error, {}", snapshot, e);
            throw new ValidateMessageException("system error");
        }
        builder.setRequestTimeout(REQUESTTIMEOUTMS);
        Request request = builder.build();
        Response response;
        try {
            response = httpClient.executeRequest(request).get();
        } catch (Exception e) {
            logger.warn("request to validate url error, {}", snapshot, e);
            throw new ValidateMessageException("request to validate url error");
        }

        if (response.getStatusCode() != Constants.OK_STATUS) {
            logger.warn("request to validate url failOf, code is [{}], {}", response.getStatusCode(), snapshot);
            throw new ValidateMessageException(
                    "request to validate url failOf, code is [" + response.getStatusCode() + "]");
        }

        VersionedJsonResponse<String> r;
        try {
            r = mapper.readValue(response.getResponseBody("utf-8"), new TypeReference<VersionedJsonResponse<String>>() {
            });
        } catch (Exception e) {
            logger.warn("get response from validate url failOf, {}", snapshot, e);
            throw new ValidateMessageException("get response from validate url failOf");
        }

        if (r.getStatus() != 0) {
            logger.warn("get response from validate url failOf, remote status is [{}], message is [{}], {}",
                    r.getStatus(), r.getMessage(), snapshot);
            throw new ValidateMessageException(
                    "get response from validate url failOf, remote message is [" + r.getMessage() + "]");
        }

        QTableError qTableError;
        try {
            qTableError = mapper.readValue(r.getData(), QTableError.class);
        } catch (IOException e) {
            logger.warn("read response from validate url failOf, {}", snapshot, e);
            throw new ValidateMessageException("read response from validate url failOf");
        }

        if (qTableError.isError()) {
            throw new ValidateErrorException(qTableError);
        }
    }

    private CandidateSnapshot getExpectedSnapshot(CandidateDTO dto, StatusType expectType)
            throws StatusMismatchException {
        CandidateSnapshot snapshot = candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(), dto.getProfile(),
                dto.getEditVersion());
        if (snapshot.getStatus() != expectType) {
            throw new StatusMismatchException();
        }
        return snapshot;
    }

    @Override
    @Transactional
    public void approve(CandidateDTO dto) {
        changeStatusAndSave(dto, StatusType.PENDING, StatusType.PASSED);
    }

    @Override
    @Transactional
    public void cancel(CandidateDTO dto) {
        lock(dto);
        changeStatusAndSave(dto, StatusType.PASSED, StatusType.CANCEL);
        unlock(dto);
    }

    @Override
    public void push(CandidateDTO dto, List<PushItemWithHostName> destinations) {
        CandidateSnapshot snapshot = getExpectedSnapshot(dto, StatusType.PASSED);
        correctData(dto, snapshot);
        ConfigMeta meta = new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile());
        notifyService.notifyPush(meta, snapshot.getEditVersion(), destinations);
    }

    // edit push暂时跳过一些校验，后面跟push可以合并
    @Override
    public void pushEditing(CandidateDTO dto, List<PushItemWithHostName> destinations) {
        CandidateSnapshot snapshot = candidateSnapshotDao
                .find(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());
        ConfigMeta meta = new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile());
        notifyService.notifyPush(meta, snapshot.getEditVersion(), destinations);
    }

    @Override
    @Transactional
    public void delete(CandidateDTO dto) {
        CandidateSnapshot oldSnapshot = candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(), dto.getProfile(),
                dto.getEditVersion());
        if (oldSnapshot.getStatus() == StatusType.DELETE) {
            throw new ModifiedException();
        }
        Candidate candidate = updateCandidate(oldSnapshot, StatusType.DELETE);
        updateCandidateMapping(dto, candidate, false);

        CandidateSnapshot snapshot = new CandidateSnapshot(candidate, oldSnapshot.getData(), userContext.getRtxId());
        candidateSnapshotDao.save(snapshot);
        fileContentMD5Service.applyConfigChange(snapshot);
        if (oldSnapshot.getStatus() == StatusType.PUBLISH) {
            configService.delete(snapshot);
        }
        if (FileChecker.isTemplateFile(dto.getDataId())) {
            fileTemplateService.deleteDefaultConfigId(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()));
        }
    }

    @Override
    @Transactional
    public CandidateSnapshot publish(CandidateDTO dto) {
        lock(dto);
        CandidateSnapshot candidateSnapshot = doPublish(dto);
        unlock(dto);

        return candidateSnapshot;
    }

    @Override
    public void greyBatchPublish(CandidateDTO dto) {
        doPublish(dto);
    }

    private void unlock(CandidateDTO dto) {
        greyReleaseService.deleteTaskMapping(getMeta(dto), dto.getUuid());
    }

    private void unlock(List<String> lockedUuidList) {
        if (!greyReleaseService.deleteTaskMapping(lockedUuidList)) {
            throw new ModificationDuringPublishingException("解锁失败，请联系管理员！");
        }
    }

    private CandidateSnapshot doPublish(CandidateDTO dto) {
        return configService.publish(changeStatusAndSave(dto, StatusType.PASSED, StatusType.PUBLISH));
    }

    private void lock(CandidateDTO dto) {
        dto.setUuid(UUIDUtil.generate());
        if (!greyReleaseService.insertTaskMapping(getMeta(dto), dto.getUuid())) {
            throw new ModificationDuringPublishingException("检测到有发布任务正在执行");
        }
    }

    private void lock(List<CandidateDTO> dtoList) {
        if (!greyReleaseService.batchInsertTaskMapping(dtoList)) {
            throw new ModificationDuringPublishingException("检测到有发布任务正在执行");
        }
    }

    private ConfigMeta getMeta(CandidateDTO dto) {
        return new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
    }

    private void correctData(CandidateDTO dto, CandidateSnapshot snapshot) {
        dto.setBasedVersion(snapshot.getBasedVersion());
        dto.setStatus(snapshot.getStatus());
    }

    @Override
    @Transactional
    public boolean makeInherit(ConfigMeta configMeta) {
        filePublicStatusService.setInherit(new ConfigMetaWithoutProfile(configMeta.getGroup(), configMeta.getDataId()));
        referenceDao.updateStatusFromRefMeta(configMeta, ReferenceStatus.NORMAL, RefType.INHERIT.value());
        return true;
    }

    @Override
    public boolean makeRest(ConfigMeta configMeta) {
        filePublicStatusService.setRest(new ConfigMetaWithoutProfile(configMeta.getGroup(), configMeta.getDataId()));
        return true;
    }

    @Override
    @Transactional
    public boolean makePublic(ConfigMeta configMeta) {
        filePublicStatusService.setPublic(new ConfigMetaWithoutProfile(configMeta.getGroup(), configMeta.getDataId()));
        referenceDao.updateStatusFromRefMeta(configMeta, ReferenceStatus.NORMAL);
        return true;
    }

    @Override
    @Transactional
    public boolean makePublic(List<CandidateDTO> candidateDTOList) {
        filePublicStatusService.batchSetPublic(candidateDTOList);
        for (CandidateDTO candidateDTO : candidateDTOList) {
            ConfigMeta configMeta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
            referenceDao.updateStatusFromRefMeta(configMeta, ReferenceStatus.NORMAL);
        }
        return true;
    }

    private boolean checkNotNull(ConfigDetail configDetail) {
        if (configDetail != null
                && configDetail.getConfigField() != null
                && configDetail.getContent() != null) {
            return true;
        }
        return false;
    }

    private ConfigInfoWithoutPublicStatus findCurrentConfig(ConfigField configField, boolean needPriority) {
        List<ConfigMeta> candidateConfigMetas = Lists.newLinkedList();
        if (needPriority) {
            candidateConfigMetas = PriorityUtil.createPriorityList(new ConfigMeta(configField.getGroup(), configField.getDataId(), configField.getProfile()));
        } else {
            candidateConfigMetas.add(new ConfigMeta(configField.getGroup(), configField.getDataId(), configField.getProfile()));
        }
        for (ConfigMeta configMeta : candidateConfigMetas) {
            ConfigInfoWithoutPublicStatus config = configService.findPublishedConfigWithoutPublicStatus(configMeta);
            if (config != null) {
                return config;
            }
        }
        return null;
    }

    private void checkConfigExists(ConfigDetail configDetail, ConfigInfoWithoutPublicStatus currentConfig) throws JsonProcessingException, FileNotFoundException {
        if (currentConfig == null) {
            throw new FileNotFoundException("该文件不存在，" + mapper.writeValueAsString(configDetail));
        }
        if (!currentConfig.getProfile().equalsIgnoreCase(configDetail.getConfigField().getProfile())
                || currentConfig.getVersion() != configDetail.getVersion()) {
            throw new IllegalStateException("文件环境或者版本对应不上，该文件可能已经被修改");
        }
    }


    private void checkSubEnv(ConfigDetail configDetail) {
        List<String> subEnvList = profileService.find(configDetail.getConfigField().getGroup());
        String maxSubenvSize = qconfigs.get("max.subenv.size");
        if (Strings.isNullOrEmpty(maxSubenvSize)) {
            maxSubenvSize = "100";
        }
        if (subEnvList != null && subEnvList.size() > Integer.valueOf(maxSubenvSize).intValue()) {
            throw new IllegalArgumentException("最多创建" + maxSubenvSize + "个子环境");
        }
        if (!profileExist(configDetail.getConfigField().getGroup(), configDetail.getConfigField().getProfile())) {//判断子环境是否存在
            String buildGroup = ProfileUtil.getBuildGroup(configDetail.getConfigField().getProfile());
            if (!Strings.isNullOrEmpty(buildGroup)) {
                profileService.create(configDetail.getConfigField().getGroup(), configDetail.getConfigField().getProfile());
            }
        }
    }

    private boolean profileExist(String group, String profile) {
        String buildGroup = ProfileUtil.getBuildGroup(profile);
        return Strings.isNullOrEmpty(buildGroup) || profileService.exist(group, profile);
    }

    private boolean needForceApply(CandidateDTO candidateDTO) {
        boolean forceApply = false;
        if (candidateDTO.getEditVersion() == INIT_BASED_VERSION) {
            Candidate candidateNow = candidateDao.find(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
            // 这是文件已经被删除而客户端进行新建的情况
            if (candidateNow != null && candidateNow.getStatus() == StatusType.DELETE) {
                forceApply = true;
                candidateDTO.setEditVersion(candidateNow.getEditVersion());
            }
        }
        return forceApply;
    }
}
