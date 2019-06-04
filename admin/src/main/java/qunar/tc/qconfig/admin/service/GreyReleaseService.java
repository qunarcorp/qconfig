package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.cloud.vo.HostPushStatusVo;
import qunar.tc.qconfig.admin.dto.BatchReleaseResult;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatus;
import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface GreyReleaseService {

    Optional<StatusInfo> queryStatus(String uuid);

    Optional<StatusInfo> queryActiveTask(ConfigMeta meta);

    Optional<String> queryActiveTaskUUID(ConfigMeta meta);

    List<StatusInfo> queryTasksNotOperatedForSeconds(int seconds);

    boolean updateTask(StatusInfo statusInfo);

    @Deprecated
    Optional<ReleaseStatus> createAndInitTask(StatusInfo statusInfo);

    Optional<BatchReleaseResult> createAndInitTasks(StatusInfo statusInfo);

    boolean finishTask(StatusInfo statusInfo);

    boolean cancelTask(StatusInfo statusInfo);

    boolean insertTaskMapping(ConfigMeta meta, String uuid);

    List<StatusInfo> queryHistoryTasks(ConfigMeta meta, long currentPage, long pageSize);

    boolean batchInsertTaskMapping(List<CandidateDTO> candidateDTOList);

    boolean deleteTaskMapping(ConfigMeta meta, String uuid);

    boolean deleteTaskMapping(List<String> uuidList);

    List<HostPushStatusVo> getServers(ConfigMeta meta, final long targetVersion) throws InterruptedException, ExecutionException, TimeoutException;

    List<HostPushStatusVo> getListeningServers(ConfigMeta meta, final long targetVersion, int timeout) throws
            InterruptedException, ExecutionException, TimeoutException;

    Map<Integer, Map<String, List<HostPushStatusVo>>> getAllPushStatus(StatusInfo statusInfo);
}
