package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.cloud.vo.EditPushHostVO;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.dao.FilePushHistoryDao;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.model.FilePushHistory;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.FileContentMD5Service;
import qunar.tc.qconfig.admin.service.PushHistoryService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.bean.PushStatus;
import qunar.tc.qconfig.admin.web.bean.PushType;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by pingyang.yang on 2018/10/22
 */
@Service
public class PushHistoryServiceImpl implements PushHistoryService {

    private static final int EMPTY_PORT = 0;

    @Resource
    private ConfigService configService;

    @Resource
    private UserContextService userContext;

    @Resource
    private FilePushHistoryDao filePushHistoryDao;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private FileContentMD5Service fileContentMD5Service;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Override
    public List<ConfigMeta> getUnRollbackPushList(String group, String profile) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group can not be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(profile), "profile can not be null");

        List<Candidate> candidates = configService.findCandidates(group, profile);
        List<ConfigMeta> resultList = Lists.newLinkedList();

        for (Candidate candidate : candidates) {
            if (!isFileContextEqualPublished(candidate.getGroup(), candidate.getDataId(), candidate.getProfile())) {
                resultList.add(new ConfigMeta(candidate.getGroup(), candidate.getDataId(), candidate.getProfile()));
            }
        }

        return resultList;
    }

    @Override
    public boolean checkIsPushRollback(ConfigMeta configMeta) {
        return isFileContextEqualPublished(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile());
    }

    @Override
    public void addOneHistory(CandidateSnapshot candidateSnapshot, List<PushItemWithHostName> pushMachine,
            PushType type) {
        List<FilePushHistory> addNeedHistory = Lists.newLinkedList();
        for (PushItemWithHostName hostName : pushMachine) {
            FilePushHistory filePushHistory = new FilePushHistory(candidateSnapshot.getGroup(),
                    candidateSnapshot.getDataId(), candidateSnapshot.getProfile(),
                    (int) candidateSnapshot.getEditVersion(),
                    ChecksumAlgorithm.getChecksum(candidateSnapshot.getData()), type, PushStatus.PUSHING,
                    hostName.getIp(), hostName.getPort(), userContext.getRtxId());
            addNeedHistory.add(filePushHistory);
        }
        filePushHistoryDao.batchInsert(addNeedHistory);
    }

    @Override
    public void addGreyReleaseHistory(ConfigMeta meta, long version, List<Host> machineList) {
        CandidateSnapshot snapshot = candidateSnapshotDao
                .find(meta.getGroup(), meta.getDataId(), meta.getProfile(), version);
        List<FilePushHistory> addNeedHistory = Lists.newLinkedList();
        for (Host temp : machineList) {
            FilePushHistory filePushHistory = new FilePushHistory(meta.getGroup(), meta.getDataId(), meta.getProfile(),
                    (int) version, ChecksumAlgorithm.getChecksum(snapshot.getData()), PushType.GERY_RELEASE,
                    PushStatus.GREY_RELEASING, temp.getIp(), EMPTY_PORT, userContext.getRtxId());
            filePushHistoryDao.insert(filePushHistory);
            addNeedHistory.add(filePushHistory);
        }
        filePushHistoryDao.batchInsert(addNeedHistory);
    }

    /**
     * 获取编辑推送会对应文件的未回滚机器
     *
     * @param configMeta 文件信息
     * @return 键为IP，值为端口的map
     */
    @Override
    public Map<String, Integer> getFileEditPushMachineIPAndPort(ConfigMeta configMeta) {
        int publishedVersion = (int) configService.getCurrentPublishedData(configMeta).getVersion();

        Map<String, FilePushHistory> historyMap = getFilePushHistoryMap(configMeta, publishedVersion);

        return Maps.transformValues(historyMap, new Function<FilePushHistory, Integer>() {
            @Override
            public Integer apply(FilePushHistory input) {
                return input.getPort();
            }
        });
    }

    private Map<String, FilePushHistory> getFilePushHistoryMap(ConfigMeta configMeta, int publishedVersion) {
        Map<String, FilePushHistory> historyMap;
        List<FilePushHistory> histories = filePushHistoryDao
                .getEditPushHistory(configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(),
                        publishedVersion);

        //获取机器的最新推送
        historyMap = filterNewestPush(histories);

        filterUnRollBackFile(configMeta, publishedVersion, historyMap);
        return historyMap;
    }

    @Override
    public List<EditPushHostVO> getUnRollBackIPandPort(ConfigMeta configMeta) {

        int publishedVersion = (int) configService.getCurrentPublishedData(configMeta).getVersion();

        Map<String, FilePushHistory> historyMap = getFilePushHistoryMap(configMeta, publishedVersion);

        return Lists.newArrayList(
                Collections2.transform(historyMap.values(), new Function<FilePushHistory, EditPushHostVO>() {
                    @Override
                    public EditPushHostVO apply(FilePushHistory input) {
                        return new EditPushHostVO(input.getIP() + ":" + input.getPort(), input.getVersion());
                    }
                }));
    }

    private Map<String, FilePushHistory> filterNewestPush(List<FilePushHistory> histories) {
        Map<String, FilePushHistory> historyMap = Maps.newHashMap();
        for (FilePushHistory temp : histories) {
            if (historyMap.containsKey(temp.getIP())) {
                if (historyMap.get(temp.getIP()).getVersion() < temp.getVersion()) {
                    historyMap.put(temp.getIP(), temp);
                }
            } else {
                historyMap.put(temp.getIP(), temp);
            }
        }
        return historyMap;
    }

    /**
     * 过滤掉已经回滚掉编辑中推送
     */
    private void filterUnRollBackFile(ConfigMeta configMeta, int publishedVersion,
            Map<String, FilePushHistory> historyMap) {
        String publishedContentMD5 = fileContentMD5Service.getFileContentMD5(configMeta, publishedVersion);
        Iterator<Map.Entry<String, FilePushHistory>> iterator = historyMap.entrySet().iterator();
        while (iterator.hasNext()) {
            FilePushHistory history = iterator.next().getValue();
            if (Objects.equal(publishedContentMD5, history.getMD5())) {
                iterator.remove();
            }
        }
    }

    @Override
    public void changeGreyReleaseStatus(ConfigMeta meta, long version, PushStatus status) {
        filePushHistoryDao.updateStatus(meta.getGroup(), meta.getDataId(), meta.getProfile(), (int) version, status);
    }

    @Override
    public void notifyGreyReleaseFinish(ConfigMeta meta, long version, List<Host> hosts) {
        for (Host temp : hosts) {
            ConfigUsedLog log = configUsedLogDao
                    .selectNewest(meta.getGroup(), meta.getDataId(), meta.getProfile(), temp.getIp());
            if (log != null && log.getVersion() >= version) {
                filePushHistoryDao
                        .updateStatus(meta.getGroup(), meta.getProfile(), meta.getDataId(), (int) version, temp.getIp(),
                                PushStatus.SUCCESS);
            } else {
                filePushHistoryDao
                        .updateStatus(meta.getGroup(), meta.getProfile(), meta.getDataId(), (int) version, temp.getIp(),
                                PushStatus.FAILED);
            }
        }
    }

    private boolean isFileContextEqualPublished(String group, String dataId, String profile) {
        int publishedVersion = (int) configService.getCurrentPublishedData(new ConfigMeta(group, dataId, profile))
                .getVersion();
        List<FilePushHistory> histories = filePushHistoryDao
                .getEditPushHistory(group, profile, dataId, publishedVersion);

        //获取机器的最新推送
        Map<String, FilePushHistory> historyMap = filterNewestPush(histories);

        filterUnRollBackFile(new ConfigMeta(group, dataId, profile), publishedVersion, historyMap);

        return historyMap.size() == 0;
    }

}
