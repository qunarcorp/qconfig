package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.cloud.vo.HostPushStatusVo;
import qunar.tc.qconfig.admin.dao.BatchPushTaskDao;
import qunar.tc.qconfig.admin.dao.BatchPushTaskMappingDao;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.dto.BatchReleaseResult;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.event.CandidateDTONotifyBean;
import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.event.CurrentConfigNotifyBean;
import qunar.tc.qconfig.admin.event.ReferenceNotifyBean;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.exception.StatusMismatchException;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseState;
import qunar.tc.qconfig.admin.greyrelease.MachinePushState;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatus;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatusFactory;
import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.admin.greyrelease.TaskConfig;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.service.ApplyQueueService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.GreyReleaseService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.PushHistoryService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.bean.PushStatus;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class GreyReleaseServiceImpl implements GreyReleaseService {

    private final static Logger logger = LoggerFactory.getLogger(GreyReleaseService.class);
    private static final String SPLIT_CHART = "/";

    @Resource
    private BatchPushTaskDao batchPushTaskDao;

    @Resource
    private BatchPushTaskMappingDao batchPushTaskMappingDao;

    @Resource
    private PushHistoryService pushHistoryService;

    @Resource
    private ApplyQueueService applyService;

    @Resource
    private UserContextService userContext;

    @Resource
    private EventBus eventBus;

    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private ConfigDao configDao;

    @Resource
    private InheritConfigDaoImpl inheritConfigDao;

    @Resource
    private ConfigService configService;

    @Resource
    private ListeningClientsService listeningClientsService;

    @Resource
    private ReleaseStatusFactory factory;

    @Override
    public Optional<StatusInfo> queryStatus(String uuid) {
        return Optional.ofNullable(batchPushTaskDao.queryTask(uuid));
    }

    @Override
    public Optional<StatusInfo> queryActiveTask(ConfigMeta meta) {
        String uuid = batchPushTaskMappingDao.selectUuid(meta);
        if (Strings.isNullOrEmpty(uuid)) {
            return Optional.empty();
        }
        return Optional.ofNullable(batchPushTaskDao.queryTask(uuid));
    }

    @Override
    public Optional<String> queryActiveTaskUUID(ConfigMeta meta) {
        return Optional.ofNullable(batchPushTaskMappingDao.selectUuid(meta));
    }

    @Override
    public List<StatusInfo> queryTasksNotOperatedForSeconds(int seconds) {
        Timestamp updateTime = new Timestamp(System.currentTimeMillis() - seconds * 1000L);
        List<String> uuids = batchPushTaskMappingDao.selectUuidsUpdateBefore(updateTime);
        return batchPushTaskDao.selectUuidIn(uuids);
    }

    @Override
    @Transactional
    public boolean updateTask(StatusInfo statusInfo) {
        if (batchPushTaskMappingDao.update(statusInfo.getUuid(), statusInfo.getLockVersion())) {
            batchPushTaskDao.updateTaskStatus(statusInfo);
            return true;
        }
        return false;
    }

    @Override
    @Deprecated
    @Transactional
    public Optional<ReleaseStatus> createAndInitTask(StatusInfo statusInfo) {
        return Optional.empty();
    }

    /**
     * 创建批量灰度的任务
     *
     * @param statusInfo 任务信息
     * @return 任务执行对象
     */
    @Override
    @Transactional
    public Optional<BatchReleaseResult> createAndInitTasks(StatusInfo statusInfo) {
        if (!checkStatusLegal(statusInfo)) {
            return Optional.empty();
        }

        String UUID = statusInfo.getUuid();

        Map<String, String> failMap = Maps.newHashMap();

        Iterator<ConfigMetaVersion> iterator = statusInfo.getTaskConfig().getMetaVersions().iterator();

        while (iterator.hasNext()) {
            ConfigMetaVersion metaVersion = iterator.next();
            try {
                dealOneMeta(UUID, failMap, iterator, metaVersion);
            } catch (ModifiedException | StatusMismatchException e) {
                logger.error(
                        "create grey publish task failOf, file modified or status mismatch, status[{}], dataId [{}] ",
                        statusInfo, metaVersion.getDataId(), e);
                failMap.put(metaVersion.getDataId(), "文件已被修改，请回退后再重试！");
                iterator.remove();
            } catch (Exception e) {
                logger.error("create grey publish task failOf, task status:[{}], dataId [{}]", statusInfo,
                        metaVersion.getDataId(), e);
                failMap.put(metaVersion.getDataId(), "创建任务失败!");
                iterator.remove();
            }
        }

        ReleaseStatus status = factory.create(statusInfo);
        ReleaseStatus nextStatus = init(status);
        StatusInfo nextStatusInfo = nextStatus.getStatusInfo();
        batchPushTaskDao.insertTask(nextStatusInfo);

        return Optional.of(new BatchReleaseResult(nextStatus, failMap));
    }

    private boolean checkStatusLegal(StatusInfo statusInfo) {
        if (statusInfo == null) {
            return false;
        }
        if (statusInfo.getTaskConfig() == null) {
            return false;
        }
        TaskConfig config = statusInfo.getTaskConfig();
        return config.getMetaVersions() != null && config.getMetaVersions().size() != 0;
    }

    /**
     * 解决事务而抽出的方法
     *
     * @param UUID 任务UUID
     * @param failMap 失败几率
     * @param iterator 当前便利器
     * @param metaVersion 当前文件
     */
    @Transactional
    void dealOneMeta(String UUID, Map<String, String> failMap, Iterator<ConfigMetaVersion> iterator, ConfigMetaVersion metaVersion) {
        ConfigMeta meta = metaVersion.getConfigMeta();
        // TODO use batch insert
        if (!batchPushTaskMappingDao.insert(meta, UUID)) {
            failMap.put(metaVersion.getDataId(), "已存在任务！");
            iterator.remove();
        }
        validate(metaVersion);
    }

    private ReleaseStatus init(ReleaseStatus releaseStatus) {
        return factory.create(releaseStatus.getStatusInfo().copy().setState(GreyReleaseState.PUBLISHING));
    }

    private void validate(ConfigMetaVersion meta) {
        Candidate candidate = configService.currentEdit(meta.getConfigMeta());
        if (candidate == null || candidate.getEditVersion() != meta.getVersion()) {
            throw new ModifiedException();
        }
        if (candidate.getStatus() != StatusType.PASSED) {
            throw new StatusMismatchException();
        }
    }

    @Override
    @Transactional
    public boolean finishTask(StatusInfo statusInfo) {
        for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {
            if (!batchPushTaskMappingDao.update(metaVersion.getDataId(), statusInfo.getUuid(), statusInfo.getLockVersion())) {
                throw new ModifiedException();
            }
            batchPushTaskMappingDao.delete(metaVersion.getConfigMeta(), statusInfo.getUuid());
        }

        batchPushTaskDao.updateTaskStatus(statusInfo);
        doFinish(statusInfo);
        notifyGreyReleaseFinish(statusInfo);
        return true;
    }

    private void notifyGreyReleaseFinish(StatusInfo statusInfo) {
        Map<Integer, List<Host>> batches = statusInfo.getBatches();
        List<Host> allHost = Lists.newArrayList();
        for (List<Host> temp : batches.values()) {
            allHost.addAll(temp);
        }
        for (ConfigMetaVersion metaVersion: statusInfo.getTaskConfig().getMetaVersions()) {
            pushHistoryService.notifyGreyReleaseFinish(metaVersion.getConfigMeta(), metaVersion.getVersion(), allHost);
        }
    }

    private boolean doFinish(StatusInfo statusInfo) {
        try {
            userContext.setIp("0.0.0.0");
            userContext.freshGroupInfos();
            for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {
                ConfigMeta meta = metaVersion.getConfigMeta();
                CandidateDTO dto = new CandidateDTO();
                dto.setUuid(statusInfo.getUuid());
                dto.setGroup(meta.getGroup());
                dto.setDataId(meta.getDataId());
                dto.setProfile(meta.getProfile());
                dto.setEditVersion(metaVersion.getVersion());
                applyService.greyBatchPublish(dto);
                postEvent(dto);
            }
            return true;
        } catch (RuntimeException e) {
            logger.error("grey publish error, {}", statusInfo, e);
            throw e;
        } finally {
            userContext.clear();
        }
    }

    private void postEvent(CandidateDTO dto) {
        try {
            loadDataAndPostEvent(dto, ConfigOperationEvent.PUBLISH, "");
        } catch (Exception e) {
            logger.error("loadDataAndPostEvent error, {}", dto, e);
        }

        try {
            postCurrentConfigChangedEvent(dto, ConfigOperationEvent.PUBLISH);
        } catch (Exception e) {
            logger.error("postCurrentConfigChangedEvent error, {}", dto, e);
        }

        try {
            loadInheritdataAndPostInheritEvent(dto);
        } catch (Exception e) {
            logger.error("loadInheritdataAndPostInheritEvent error, {}", dto, e);
        }
    }

    private void loadDataAndPostEvent(CandidateDTO dto, ConfigOperationEvent event, String remarks) {
        dto.setData(candidateSnapshotDao.find(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion())
                .getData());
        final CandidateDTONotifyBean notifyBean = new CandidateDTONotifyBean(event, userContext.getRtxId(),
                userContext.getIp(), dto, remarks, userContext.getApplication(dto.getGroup()));
        eventBus.post(notifyBean);
    }

    private void postCurrentConfigChangedEvent(final CandidateDTO dto, final ConfigOperationEvent event) {
        if (event != ConfigOperationEvent.DELETE && event != ConfigOperationEvent.PUBLISH) {
            // 只处理发布和删除两种情况
            return;
        }

        final ConfigMeta configMeta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        final CandidateSnapshot snapshot = configDao.findCurrentSnapshot(configMeta);
        if (snapshot == null) {
            return;
        }
        snapshot.setOperator(userContext.getRtxId());
        snapshot.setApplication(userContext.getApplication(snapshot.getGroup()));
        asyncEventBus.post(new CurrentConfigNotifyBean(event, snapshot));
    }

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
    @Transactional
    public boolean cancelTask(StatusInfo statusInfo) {
        if (batchPushTaskMappingDao.update(statusInfo.getUuid())) {
            if (batchPushTaskDao.updateTaskStatus(statusInfo, GreyReleaseState.CANCEL, GreyReleaseState.FINISH)) {
                for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {
                    batchPushTaskMappingDao.delete(metaVersion.getConfigMeta(), statusInfo.getUuid());
                    pushHistoryService.changeGreyReleaseStatus(metaVersion.getConfigMeta(), metaVersion.getVersion(), PushStatus.CANCEL);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean insertTaskMapping(ConfigMeta meta, String uuid) {
        return batchPushTaskMappingDao.insert(meta, uuid);
    }

    @Override
    public boolean batchInsertTaskMapping(List<CandidateDTO> candidateDTOList) {
        return (batchPushTaskMappingDao.batchSave(candidateDTOList) == candidateDTOList.size());
    }

    @Override
    public List<StatusInfo> queryHistoryTasks(ConfigMeta meta, long currentPage, long pageSize) {
        return batchPushTaskDao.queryHistoryTasks(meta, currentPage, pageSize);
    }

    @Override
    public boolean deleteTaskMapping(ConfigMeta meta, String uuid) {
        return batchPushTaskMappingDao.delete(meta, uuid);
    }

    @Override
    public boolean deleteTaskMapping(List<String> uuidList) {
        return batchPushTaskMappingDao.delete(uuidList);
    }

    @Override
    public List<HostPushStatusVo> getServers(ConfigMeta meta, final long targetVersion) throws InterruptedException, ExecutionException, TimeoutException {

        ListenableFuture<Set<ClientData>> clientsDataFuture = listeningClientsService.getListeningClientsData(meta, true);
        Set<ClientData> clientDataSet = clientsDataFuture.get(Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<HostPushStatusVo> hostPushStatus = Lists.newArrayListWithCapacity(clientDataSet.size());
        for (ClientData clientData : clientDataSet) {
            MachinePushState status = clientData.getVersion() >= targetVersion ? MachinePushState.success : MachinePushState.waiting;
            hostPushStatus.add(new HostPushStatusVo(clientData.getIp(), status));
        }
        return hostPushStatus;

    }

    @Override
    public List<HostPushStatusVo> getListeningServers(ConfigMeta meta, final long targetVersion, int timeout) throws
        InterruptedException, ExecutionException, TimeoutException {

        ListenableFuture<Set<ClientData>> clientsDataFuture = listeningClientsService.getListeningClientsData(meta, true);
        Set<ClientData> clientDataSet = clientsDataFuture.get(timeout, TimeUnit.SECONDS);
        List<HostPushStatusVo> hostPushStatus = Lists.newArrayListWithCapacity(clientDataSet.size());
        for (ClientData clientData : clientDataSet) {
            MachinePushState state = MachinePushState.waiting;
            if (clientData != null && clientData.getVersion() >= targetVersion) {
                state = MachinePushState.success;
            }
            hostPushStatus.add(new HostPushStatusVo(clientData.getIp(), state));
        }
        return hostPushStatus;
    }

    private Set<String> getSuccessIpSet(ConfigMetaVersion meta) throws InterruptedException, ExecutionException, TimeoutException {
        Set<String> successIpSet = Sets.newHashSet();

        ListenableFuture<Set<ClientData>> listeningClientsData = listeningClientsService.getListeningClientsData(meta.getConfigMeta(), true);
        Set<ClientData> clientDataSet = listeningClientsData.get(Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        for (ClientData clientData : clientDataSet) {
            if (clientData.getVersion() >= meta.getVersion()) {
                successIpSet.add(clientData.getIp());
            }
        }

        return successIpSet;
    }

    /**
     * 获取当前推送状态
     *
     * @param statusInfo 灰度内容
     * @return 推送内容
     */
    @Override
    public Map<Integer, Map<String, List<HostPushStatusVo>>> getAllPushStatus(StatusInfo statusInfo) {
        TaskConfig taskConfig = statusInfo.getTaskConfig();

        Map<Integer, List<Host>> hosts = statusInfo.getBatches();
        Map<Integer, Map<String, List<HostPushStatusVo>>> batchHostStatusMap = Maps
                .newHashMapWithExpectedSize(hosts.size());

        //为每个批次分别加入map
        for (Map.Entry<Integer, List<Host>> entry : hosts.entrySet()) {
            batchHostStatusMap.put(entry.getKey(), Maps.newHashMapWithExpectedSize(
                    taskConfig.getMetaVersions().size()));
        }

        //以文件为维度进行循环
        for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {
            fileStatusDeal(statusInfo, hosts, batchHostStatusMap, metaVersion);
        }
        return batchHostStatusMap;
    }

    /**
     * 处理文件推送状况
     *
     * @param statusInfo         推送内容
     * @param hosts              批次情况(批次号-机器列表)
     * @param batchHostStatusMap 批量推送结果
     * @param metaVersion        当前文件的信息
     */
    private void fileStatusDeal(StatusInfo statusInfo, Map<Integer, List<Host>> hosts,
            Map<Integer, Map<String, List<HostPushStatusVo>>> batchHostStatusMap, ConfigMetaVersion metaVersion) {
        ConfigMeta meta = metaVersion.getConfigMeta();
        Set<String> successIpSet = Sets.newHashSet();
        try {
            successIpSet = getSuccessIpSet(metaVersion);
        } catch (Exception e) {
            // ignore
        }
        //对于一个文件的内容进行分批次的循环处理
        for (Map.Entry<Integer, List<Host>> entry : hosts.entrySet()) {
            // 文件名-机器状态列表
            Map<String, List<HostPushStatusVo>> currentMap = batchHostStatusMap.get(entry.getKey());
            List<HostPushStatusVo> hostStatus;
            if (currentMap.get(getMetaConfigMapKey(meta)) == null) {
                hostStatus = Lists.newArrayListWithCapacity(entry.getValue().size());
                currentMap.put(getMetaConfigMapKey(meta), hostStatus);
            } else {
                hostStatus = currentMap.get(getMetaConfigMapKey(meta));
            }
            MachinePushState defaultPushState = statusInfo.getFinishedBatchNum() >= entry.getKey() ? MachinePushState.fail : MachinePushState.waiting;
            for (Host host : entry.getValue()) {
                MachinePushState status = successIpSet.contains(host.getIp()) ? MachinePushState.success : defaultPushState;
                hostStatus.add(new HostPushStatusVo(host.getIp(), status));
            }
        }
    }

    private String getMetaConfigMapKey(ConfigMeta meta) {
        return meta.getGroup() + SPLIT_CHART + meta.getProfile() + SPLIT_CHART + meta.getDataId();
    }
}
