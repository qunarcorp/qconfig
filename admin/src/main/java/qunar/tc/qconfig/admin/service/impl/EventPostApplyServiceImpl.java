package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.event.*;
import qunar.tc.qconfig.admin.exception.*;
import qunar.tc.qconfig.admin.model.*;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.*;
import qunar.tc.qconfig.admin.web.bean.ConfigDetail;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2014 2014/5/26 16:58
 */
@Service
public class EventPostApplyServiceImpl implements EventPostApplyService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());;

    @Resource
    private ApplyQueueService applyService;

    private static final long INIT_BASED_VERSION = 0;

    @Resource
    private EventBus eventBus;

    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private PushHistoryService pushHistoryService;

    @Resource
    private UserContextService userContext;

    @Resource
    private ConfigDao configDao;

    @Resource
    private InheritConfigDaoImpl inheritConfigDao;

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;


    @Override
    public void apply(CandidateDTO dto, String remarks)
            throws ModifiedException, StatusMismatchException, ConfigExistException, TemplateChangedException,
            IllegalTemplateException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ApplyResult result = applyService.apply(dto);
            postApplyEvent(dto, remarks, result);
        } finally {
            Monitor.applyTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void copyApply(CandidateDTO dto, ConfigInfoWithoutPublicStatus srcInfo, String remarks)
            throws ModifiedException, StatusMismatchException, ConfigExistException, TemplateChangedException,
            IllegalTemplateException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ApplyResult result = applyService.apply(dto);
            postCopyApplyEvent(dto, srcInfo, remarks, result);
        } finally {
            Monitor.applyTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public InterData oneButtonPublish(CandidateDTO dto, String remarks, boolean isForceApply)
            throws StatusMismatchException, ConfigExistException, ModifiedException, TemplateChangedException,
            IllegalTemplateException, OnePersonPublishException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            InterData interData = applyService.oneButtonPublish(dto, isForceApply);
            interData.getPublishDto().setSendMail(true);
            oneButtonPublishEvent(interData, dto, remarks);
            return interData;
        } finally {
            Monitor.normalOpTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int batchSave(List<ConfigDetail> configDetails, boolean isPublic, boolean withPriority) throws FileNotFoundException, JsonProcessingException {

        List<InterData> result = applyService.internalBatchSave(configDetails, isPublic, withPriority);
        if (result != null) {
            for (final InterData interData : result) {
                if (interData == null || interData.getPublishDto() == null) {
                    continue;
                }

                interData.setPublic(isPublic);
                oneButtonPublishEvent(interData, interData.getPublishDto(), "batch save event publish");
                if (isPublic) {
                    CandidateDTO publishedDto = interData.getPublishDto();
                    if (publishedDto.getBasedVersion() == INIT_BASED_VERSION) { // 只有新建的公共文件需要事件通知
                        postPublicConfigEvent(new ConfigMeta(publishedDto.getGroup(),
                                publishedDto.getDataId(), publishedDto.getProfile()), "make public event");
                    }
                }
            }
            return result.size();
        }
        return 0;

    }

    @Override
    public void oneButtonPublishEvent(InterData interData, CandidateDTO dto, String remarks) {
        fixVersion(dto);
        long sts = System.currentTimeMillis();
        postApplyEvent(interData.getApplyDto(), remarks, interData.getApplyResult());
        long endPostApplyEvent = System.currentTimeMillis();
        postApproveEvent(interData.getApproveDto(), remarks);
        long endPostApproveEvent = System.currentTimeMillis();
        loadDataAndPostEvent(interData.getPublishDto(), ConfigOperationEvent.PUBLISH, remarks);
        long endLoadDataAndPostEvent = System.currentTimeMillis();
        postCurrentConfigChangedEvent(interData.getPublishDto(), ConfigOperationEvent.PUBLISH);
        long endPostCurrentConfigChangedEvent = System.currentTimeMillis();

        if (!interData.isPublic()) { // 公共文件不可能是引用或继承文件
            loadInheritdataAndPostInheritEvent(dto);
        }
        long ets = System.currentTimeMillis();

        logger.info("total:{}, postApplyEvent:{},postApproveEvent:{},loadDataAndPostEvent:{},postCurrentConfigChangedEvent:{},loadInheritdataAndPostInheritEvent:{}",
                ets - sts, endPostApplyEvent - sts, endPostApproveEvent - endPostApplyEvent, endLoadDataAndPostEvent - endPostApproveEvent, endPostCurrentConfigChangedEvent - endLoadDataAndPostEvent, ets - endPostCurrentConfigChangedEvent);
    }

    private void fixVersion(CandidateDTO dto) {
        dto.setEditVersion(dto.getEditVersion() - 1);
    }

    @Override
    public void forceApply(CandidateDTO dto, String remarks)
            throws StatusMismatchException, ModifiedException, TemplateChangedException, IllegalTemplateException {
        applyService.forceApply(dto);
        eventBus.post(
                new CandidateDTONotifyBean(ConfigOperationEvent.NEW, userContext.getRtxId(), userContext.getIp(), dto,
                        remarks, userContext.getApplication(dto.getGroup())));
    }

    private void postApplyEvent(CandidateDTO dto, String remarks, ApplyResult result) {
        if (result == ApplyResult.NEW) {
            eventBus.post(
                    new CandidateDTONotifyBean(ConfigOperationEvent.NEW, userContext.getRtxId(), userContext.getIp(),
                            dto, remarks, userContext.getApplication(dto.getGroup())));
        } else if (result == ApplyResult.UPDATE) {
            eventBus.post(
                    new CandidateDTONotifyBean(ConfigOperationEvent.UPDATE, userContext.getRtxId(), userContext.getIp(),
                            dto, remarks, userContext.getApplication(dto.getGroup())));
        }
    }

    private void postCopyApplyEvent(CandidateDTO dto, ConfigInfoWithoutPublicStatus srcInfo, String remarks,
            ApplyResult result) {
        String copyRemark = String
                .format("从%s/%s/%s[版本:%s]拷贝", srcInfo.getGroup(), srcInfo.getProfile(), srcInfo.getDataId(),
                        srcInfo.getVersion());
        eventBus.post(
                new CandidateDTONotifyBean(ConfigOperationEvent.COPY, userContext.getRtxId(), userContext.getIp(), dto,
                        copyRemark, userContext.getApplication(dto.getGroup())));
        postApplyEvent(dto, remarks, result);
    }

    @Override
    public void reject(CandidateDTO dto, String remarks)
            throws ModifiedException, StatusMismatchException, OnePersonPublishException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            applyService.reject(dto);
            eventBus.post(
                    new CandidateDTONotifyBean(ConfigOperationEvent.REJECT, userContext.getRtxId(), userContext.getIp(),
                            dto, remarks, userContext.getApplication(dto.getGroup())));
        } finally {

            Monitor.normalOpTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void approve(CandidateDTO dto, String remarks)
            throws ModifiedException, StatusMismatchException, OnePersonPublishException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            applyService.approve(dto);
            postApproveEvent(dto, remarks);
        } finally {

            Monitor.normalOpTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void postApproveEvent(CandidateDTO dto, String remarks) {
        eventBus.post(
                new CandidateDTONotifyBean(ConfigOperationEvent.APPROVE, userContext.getRtxId(), userContext.getIp(),
                        dto, remarks, userContext.getApplication(dto.getGroup())));
    }

    @Override
    public void cancel(CandidateDTO dto, String remarks)
            throws ModifiedException, StatusMismatchException, OnePersonPublishException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            applyService.cancel(dto);
            eventBus.post(
                    new CandidateDTONotifyBean(ConfigOperationEvent.CANCEL, userContext.getRtxId(), userContext.getIp(),
                            dto, remarks, userContext.getApplication(dto.getGroup())));
        } finally {
            Monitor.normalOpTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void publish(CandidateDTO dto, String remarks)
            throws ModifiedException, StatusMismatchException, OnePersonPublishException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            CandidateSnapshot snapshot = applyService.publish(dto);
            dto.setData(snapshot.getData());
            dto.setSendMail(true);
            loadDataAndPostEvent(dto, ConfigOperationEvent.PUBLISH, remarks);
            postCurrentConfigChangedEvent(dto, ConfigOperationEvent.PUBLISH);
            loadInheritdataAndPostInheritEvent(dto);
        } finally {

            Monitor.publishTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void delete(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException {
        applyService.delete(dto);
        loadDataAndPostEvent(dto, ConfigOperationEvent.DELETE, remarks);
        postCurrentConfigChangedEvent(dto, ConfigOperationEvent.DELETE);
    }

    private void loadDataAndPostEvent(CandidateDTO dto, ConfigOperationEvent event, String remarks) {
        if (dto.getData() == null) {
            dto.setData(candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion())
                    .getData());
        }

        final CandidateDTONotifyBean notifyBean = new CandidateDTONotifyBean(event,
                userContext.getRtxId(),
                userContext.getIp(),
                dto,
                remarks,
                userContext.getApplication(dto.getGroup()));
        eventBus.post(notifyBean);
    }

    public void postCurrentConfigChangedEvent(final CandidateDTO dto, final ConfigOperationEvent event) {
        if (event != ConfigOperationEvent.DELETE && event != ConfigOperationEvent.PUBLISH) {
            // 只处理发布和删除两种情况
            return;
        }

        final ConfigMeta configMeta = new ConfigMeta(dto.getGroup(),
                dto.getDataId(),
                dto.getProfile());
        CandidateSnapshot snapshot;
        if (dto.getData() == null) {
            snapshot = configDao.findCurrentSnapshot(configMeta);
            if (snapshot == null) {
                return;
            }
        } else {
            snapshot = new CandidateSnapshot(new Candidate(dto.getGroup(), dto.getDataId(), dto.getProfile(),
                    dto.getBasedVersion(), dto.getEditVersion() + 1), dto.getData(), "");
        }

        snapshot.setOperator(userContext.getRtxId());
        snapshot.setApplication(userContext.getApplication(snapshot.getGroup()));
        asyncEventBus.post(new CurrentConfigNotifyBean(event, snapshot));
    }

    /**
     * notify 继承关系建立
     */
    private void loadInheritdataAndPostInheritEvent(CandidateDTO dto) {
        ConfigMeta inheritedMeta = inheritConfigDao
                .findReference(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()),
                        RefType.INHERIT.value());
        if (inheritedMeta != null) {
            Reference reference = new Reference(dto.getGroup(), dto.getProfile(), dto.getDataId(),
                    inheritedMeta.getGroup(), inheritedMeta.getProfile(), inheritedMeta.getDataId(),
                    userContext.getRtxId(), new Timestamp(System.currentTimeMillis()));
            eventBus.post(new ReferenceNotifyBean(reference, RefChangeType.ADD));
        }
    }

    @Override
    public void push(CandidateDTO dto, String remark, List<PushItemWithHostName> destinations)
            throws StatusMismatchException {
        applyService.push(dto, destinations);
        eventBus.post(new CandidateDTOPushNotifyBean(
                new CandidateDTONotifyBean(ConfigOperationEvent.PUSH, userContext.getRtxId(), userContext.getIp(), dto,
                        remark, userContext.getApplication(dto.getGroup())), destinations));
    }

    @Override
    public void pushEditing(CandidateDTO dto, String remark, List<PushItemWithHostName> destinations)
            throws StatusMismatchException {
        applyService.pushEditing(dto, destinations);
        eventBus.post(new CandidateDTOPushNotifyBean(
                new CandidateDTONotifyBean(ConfigOperationEvent.PUSH, userContext.getRtxId(), userContext.getIp(), dto,
                        remark, userContext.getApplication(dto.getGroup())), destinations));
    }

    @Override
    public void rollBackEditPush(CandidateDTO dto) throws StatusMismatchException {
        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        CandidateSnapshot current = candidateSnapshotDao.findLatestCandidateSnapshot(meta);
        if (dto.getEditVersion() != current.getEditVersion()) {
            throw new ModifiedException();
        }
        if (current.getStatus().equals(StatusType.PASSED)) {
            throw new StatusMismatchException();
        }
        Map<String, Integer> ipAndPort = pushHistoryService
                .getFileEditPushMachineIPAndPort(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()));

        if (ipAndPort.size() == 0) {
            return;
        }

        CandidateSnapshot publishedSnapshot = configDao.findCurrentSnapshot(meta);
        dto.setData(publishedSnapshot.getData());
        apply(dto, "rollbackEditPush");
        //这里加一是为了修正保存后的版本
        dto.setEditVersion(dto.getEditVersion() + 1);
        List<PushItemWithHostName> target = Lists.newArrayListWithExpectedSize(ipAndPort.size());
        for (Map.Entry<String, Integer> entry : ipAndPort.entrySet()) {
            target.add(new PushItemWithHostName(entry.getKey(), entry.getValue(), dto.getGroup(), dto.getDataId(),
                    dto.getProfile()));
        }
        applyService.pushEditing(dto, target);
        eventBus.post(new CandidateDTOPushNotifyBean(
                new CandidateDTONotifyBean(ConfigOperationEvent.PUSH, userContext.getRtxId(), userContext.getIp(), dto,
                        "rollbackEditPush", userContext.getApplication(dto.getGroup())), target));
    }

    @Override
    public boolean makePublic(ConfigMeta configMeta, String remarks) {
        applyService.makePublic(configMeta);
        postPublicConfigEvent(configMeta, remarks);
        return true;
    }

    @Override
    public void postPublicConfigEvent(ConfigMeta configMeta, String remarks) {
        postPublicStatusChangeEvent(configMeta, remarks, ConfigOperationEvent.MAKE_PUBLIC);
    }

    @Transactional
    @Override
    public void rollBack(CandidateDTO dto, boolean isApprove) {
        apply(dto, "");
        dto.setEditVersion(dto.getEditVersion() + 1);
        CandidateSnapshot candidate = candidateSnapshotDao
                .find(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());
        dto.setStatus(candidate.getStatus());
        if (isApprove && permissionService.hasPermission(dto.getGroup(), dto.getProfile(), PermissionType.APPROVE)) {
            approve(dto, "");
        }
    }

    @Override
    public boolean makeInherit(ConfigMeta configMeta, String remarks) {
        applyService.makeInherit(configMeta);
        postPublicStatusChangeEvent(configMeta, remarks, ConfigOperationEvent.MAKE_INHERIT);
        return true;
    }

    @Override
    public boolean makeRest(ConfigMeta configMeta, String remarks) {
        applyService.makeRest(configMeta);
        postPublicStatusChangeEvent(configMeta, remarks, ConfigOperationEvent.MAKE_REST);
        return true;
    }

    private void postPublicStatusChangeEvent(ConfigMeta configMeta, String remarks, ConfigOperationEvent changeEvent) {
        long version = configDao.findCurrentConfigInfo(configMeta).getVersion();
        long basedVersion = candidateSnapshotDao
                .find(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), version)
                .getBasedVersion();
        eventBus.post(new PublicStatusNotifyBean(changeEvent, configMeta, basedVersion, version, remarks,
                userContext.getRtxId(), userContext.getIp()));
    }


}

