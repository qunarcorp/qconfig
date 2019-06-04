package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.BatchGreyReleaseTaskVo;
import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.cloud.vo.GreyReleaseRequest;
import qunar.tc.qconfig.admin.cloud.vo.HostPushStatusVo;
import qunar.tc.qconfig.admin.cloud.vo.TaskStatusVo;
import qunar.tc.qconfig.admin.dto.BatchReleaseResult;
import qunar.tc.qconfig.admin.greyrelease.Command;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseState;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseUtil;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatus;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatusFactory;
import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.admin.greyrelease.TaskConfig;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.GreyReleaseService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.UUIDUtil;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/qconfig/greyRelease")
public class GreyReleaseController extends AbstractControllerHelper {

    private final static Logger logger = LoggerFactory.getLogger(GreyReleaseController.class);

    private final static int NO_CURRENT_TASK = 1;
    private final static int NO_HISTORY_TASK = 1;
    private static final int FIRST_INDEX = 0;
    private static final String EMPTY_DATA_ID = "emptyDataId";

    @Resource
    private GreyReleaseService greyReleaseService;

    @Resource
    private ReleaseStatusFactory releaseStatusFactory;

    @Resource
    private ConfigService configService;

    @Resource
    private UserContextService userContext;

    @RequestMapping(value = "/currentTask", method = RequestMethod.GET)
    @ResponseBody
    public Object queryCurrentTask(@RequestParam String group, @RequestParam String dataId, @RequestParam String profile) {
        logger.info("query current task, group:[{}], dataId:[{}], profile:[{}]", group, dataId, profile);
        try {
            Optional<String> uuid = greyReleaseService.queryActiveTaskUUID(new ConfigMeta(group, dataId, profile));
            if (uuid.isPresent()) {
                return JsonV2.successOf("", ImmutableMap.of("uuid", uuid.get()));
            } else {
                return JsonV2.successOf(NO_CURRENT_TASK, "当前没有发布任务", null);
            }
        } catch (Exception e) {
            logger.error("查询当前任务失败,group:[{}], dataId:[{}], profile:[{}]", group, dataId, profile, e);
            return JsonV2.failOf("get current task error");
        }
    }

    @RequestMapping(value = "/servers", method = RequestMethod.GET)
    @ResponseBody
    public Object listServers(@RequestParam String group, @RequestParam String profile, @RequestParam String dataId,
                             @RequestParam long version) {
        logger.info("get servers list, group:[{}], dataId:[{}], profile:[{}], version:[{}]", group, dataId, profile, version);
        try {
            return JsonV2.successOf(
                    greyReleaseService.getListeningServers(
                            new ConfigMeta(group, dataId, profile),
                            version,
                            Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS));
        } catch (Exception e) {
            logger.error("获取server列表失败, group:[{}], dataId:[{}], profile:[{}]", group, dataId, profile);
            return JsonV2.failOf("获取server列表失败");
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public Object getTaskStatus(@RequestParam String uuid) {
        UUIDUtil.checkValid(uuid);
        try {
            Optional<StatusInfo> statusInfoOptional = greyReleaseService.queryStatus(uuid);
            if (!statusInfoOptional.isPresent()) {
                logger.error("task uuid:[{}] not exist", uuid);
                return JsonV2.failOf(String.format("任务uuid:%s不存在!", uuid));
            }
            StatusInfo statusInfo = statusInfoOptional.get();
            Map<Integer, Map<String, List<HostPushStatusVo>>> batchStatus = greyReleaseService
                    .getAllPushStatus(statusInfo);
            TaskStatusVo resultInfo = new TaskStatusVo.Builder()
                    .setInfo(statusInfo)
                    .setBatchStatus(batchStatus)
                    .build();
            return JsonV2.successOf(resultInfo);
        } catch (Exception e) {
            logger.error("getTaskStatus error, uuid[{}]", uuid, e);
            return JsonV2.failOf("getTaskStatus error");
        }

    }

    @RequestMapping(value = "/pause", method = RequestMethod.POST)
    @ResponseBody
    public Object pauseTask(@RequestBody GreyReleaseRequest request) {
        logger.info("pause task, uuid:[{}]", request.getUuid());
        try {
            Optional<StatusInfo> statusInfoOptional = greyReleaseService.queryStatus(request.getUuid());
            if (!statusInfoOptional.isPresent()) {
                logger.error("task uuid:[{}] not exist", request.getUuid());
                return JsonV2.failOf(String.format("任务uuid:%s不存在!", request.getUuid()));
            }
            StatusInfo statusInfo = statusInfoOptional.get();
            Preconditions.checkArgument(statusInfo.getState() != GreyReleaseState.FINISH, "该任务已完成");
            Preconditions.checkArgument(statusInfo.getState() != GreyReleaseState.CANCEL, "该任务已取消");
            if (!releaseStatusFactory.create(statusInfo).accept(Command.pause).isPresent()) {
                throw new RuntimeException("pause task error");
            }
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("pause batch push task error, uuid:[{}]", request.getUuid(), e);
            return JsonV2.failOf("暂停任务失败," + e.getMessage());
        }
    }

    @RequestMapping(value = "/continue", method = RequestMethod.POST)
    @ResponseBody
    public Object continueTask(@RequestBody GreyReleaseRequest request) {
        logger.info("continue task, uuid:[{}], lockVersion:[{}]", request.getUuid(), request.getLockVersion());
        try {
            Optional<StatusInfo> statusInfoOptional = greyReleaseService.queryStatus(request.getUuid());
            if (!statusInfoOptional.isPresent()) {
                return JsonV2.failOf(String.format("任务uuid:%s不存在!", request.getUuid()));
            }
            StatusInfo statusInfo = statusInfoOptional.get();
            Preconditions.checkArgument(statusInfo.getLockVersion() == request.getLockVersion(), "任务状态已被修改，请刷新后重试");
            Preconditions.checkArgument(statusInfo.getState() != GreyReleaseState.FINISH, "该任务已完成");
            Preconditions.checkArgument(statusInfo.getState() != GreyReleaseState.CANCEL, "该任务已取消");
            if (!releaseStatusFactory.create(statusInfo).accept(Command.next).isPresent()) {
                throw new RuntimeException("continue task error");
            }
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("continue push task error, uuid:[{}]", request.getUuid(), e);
            return JsonV2.failOf("继续执行失败," + e.getMessage());
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public Object cancelTask(@RequestBody GreyReleaseRequest request) {
        logger.info("cancel task, uuid:[{}]", request.getUuid());
        try {
            Optional<StatusInfo> statusInfo = greyReleaseService.queryStatus(request.getUuid());
            if (!statusInfo.isPresent()) {
                return JsonV2.failOf(String.format("任务uuid:%s不存在!", request.getUuid()));
            }
            Preconditions.checkArgument(statusInfo.get().getState() != GreyReleaseState.FINISH, "该任务已完成");
            Preconditions.checkArgument(statusInfo.get().getState() != GreyReleaseState.CANCEL, "该任务已取消");
            if (!releaseStatusFactory.create(statusInfo.get()).accept(Command.cancel).isPresent()) {
                throw new RuntimeException("cancel task error");
            }
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("cancel push task error, uuid:[{}]", request.getUuid(), e);
            return JsonV2.failOf("取消任务失败," + e.getMessage());
        }
    }


    @RequestMapping(value = "/history")
    @ResponseBody
    public Object queryHistoryTask(@RequestParam String group, @RequestParam String profile,
                                   @RequestParam(required = false, defaultValue = "1") int page,
                                   @RequestParam(required = false, defaultValue = "10") int pageSize) {
        logger.info("query history task, group:[{}],profile:[{}], page:[{}], pageSize:[{}]",
                group,  profile, page, pageSize);
        // 批量版本上线后，不再需要dataId作为历史的查询条件
        ConfigMeta meta = new ConfigMeta(group, EMPTY_DATA_ID, profile);
        checkLegalMeta(meta);
        Preconditions.checkArgument(page >= 0, "page必须大于0");
        Preconditions.checkArgument(pageSize > 0, "pageSize必须大于0");
        PaginationResult<TaskStatusVo> result = new PaginationResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        try {
            List<StatusInfo> historyStatusInfo = greyReleaseService.queryHistoryTasks(meta, page, pageSize);
            if (CollectionUtils.isEmpty(historyStatusInfo)) {
                result.setData(ImmutableList.of());
                return JsonV2.successOf(NO_HISTORY_TASK, "未查询到历史发布任务", result);
            } else {
                List<TaskStatusVo> historyTaskVos = Lists.newArrayListWithCapacity(historyStatusInfo.size());
                for (StatusInfo statusInfo: historyStatusInfo) {
                    historyTaskVos.add(new TaskStatusVo.Builder().setInfo(statusInfo).setBatchesAllocation(statusInfo.getBatches()).build());
                }
                result.setData(historyTaskVos);
                return JsonV2.successOf(result);
            }
        } catch (Exception e) {
            logger.error("query history task error, configMeta:[{}], page:[{}], pageSize:[{}]", meta, page, pageSize, e);
            return JsonV2.failOf("查询发布历史失败!");
        }
    }

    @RequestMapping("/batchCreate")
    @ResponseBody
    public Object batchCreateTask(@RequestBody BatchGreyReleaseTaskVo request) {

        Map<String, String> failList = Maps.newHashMap();
        checkAndProcessRequest(request, failList);
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(2);

        if (request.getReleaseList().size() == 0) {
            resultMap.put("failList", failList);
            return new JsonV2<>(-1, "没有文件可以发布!", resultMap);
        }

        StatusInfo info = genInitStatusInfo(request);

        resultMap.put("uuid", info.getUuid());
        //根据获取的信息，生成需要的工作
        Optional<BatchReleaseResult> releaseResult = greyReleaseService.createAndInitTasks(info);
        // 返回的内容为空
        if (!releaseResult.isPresent()) {
            resultMap.put("failList", failList);
            return JsonV2.successOf(resultMap);
        }

        ReleaseStatus statuses = releaseResult.get().getReleaseStatuses();

        // 运行获取结果
        try {
            statuses.work();
        } catch (Exception e) {
            logger.error("continue init task work error, {}", statuses, e);
        }

        failList.putAll(releaseResult.get().getFailMap());
        resultMap.put("failList", failList);
        return JsonV2.successOf(resultMap);
    }

    /**
     * 初步校验发布文件
     * 去掉不可发布的文件
     *
     * @param request  请求内容
     * @param failList 记录失败文件和原因
     */
    private void checkAndProcessRequest(@RequestBody BatchGreyReleaseTaskVo request, Map<String, String> failList) {

        List<ConfigMetaVersion> fileList = request.getReleaseList();
        Preconditions.checkArgument(fileList != null && fileList.size() > 0, "文件不能为空!");
        logger.info("create grey publish task, request:[{}]", request);

        //校验内容，同时获取一些信息
        Iterator<ConfigMetaVersion> iterator = fileList.iterator();
        while (iterator.hasNext()) {
            ConfigMetaVersion meta = iterator.next();
            CandidateSnapshot newestCandidate = configService.findLastCandidateSnapshot(meta.getConfigMeta());

            if (meta.getVersion() < newestCandidate.getEditVersion()) {
                failList.put(meta.getDataId(), "文件不是最新!");
                iterator.remove();
                continue;
            }

            CandidateSnapshot oldSnapshot = configService
                    .getCandidateDetails(meta.getGroup(), meta.getDataId(), meta.getProfile(), meta.getVersion());

            if (oldSnapshot == null) {
                failList.put(meta.getDataId(), "文件不存在!");
                iterator.remove();
                continue;
            }
            if (oldSnapshot.getStatus() == StatusType.PUBLISH) {
                failList.put(meta.getDataId(), "该文件已发布，不能再次发布!");
                iterator.remove();
                continue;
            }
            if (oldSnapshot.getStatus() != StatusType.PASSED) {
                failList.put(meta.getDataId(), "该文件尚未审核通过，不能发布!");
                iterator.remove();
            }
        }
    }

    /**
     * 处理并生成StatusInfo
     *
     * @param request 请求内容
     * @return 请求相关的请求内容
     */
    private StatusInfo genInitStatusInfo(@RequestBody BatchGreyReleaseTaskVo request) {
        TaskConfig config = checkAndParseTaskInfo(request);
        ConfigMetaVersion metaVersion = request.getReleaseList().get(FIRST_INDEX);
        checkLegalMeta(metaVersion.getConfigMeta());
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setUuid(UUIDUtil.generate());
        statusInfo.setTaskConfig(config);
        statusInfo.setLockVersion(0);
        int finishedBatchNum = request.isIncludeBastion() ?
                GreyReleaseUtil.INIT_FINISHED_BATCH_NUM :
                GreyReleaseUtil.INIT_FINISHED_BATCH_NUM + 1;
        statusInfo.setFinishedBatchNum(finishedBatchNum);
        statusInfo.setState(GreyReleaseState.WAIT_PUBLISH);
        Date now = new Date();
        statusInfo.setCreateTime(now);
        statusInfo.setUpdateTime(now);
        statusInfo.setLastPushTime(now);
        statusInfo.setOperator(userContext.getRtxId());
        return statusInfo;
    }

    /**
     * 校验并获取任务中相关的设置
     *
     * @param request 请求内容
     * @return 任务设置
     */
    private TaskConfig checkAndParseTaskInfo(BatchGreyReleaseTaskVo request) {
        Preconditions.checkArgument(request.getTotalBatchNum() != null && request.getTotalBatchNum() > 0, "批次数必须大于0");
        Preconditions.checkArgument(
                !request.isAutoContinue() || (request.getBatchInterval() != null && request.getBatchInterval() > 0),
                "指定自动执行时，需指定批次间隔时间，且应大于0");
        Map<Integer, List<Host>> batchMap = request.getBatchMap();
        Preconditions.checkArgument(!CollectionUtils.isEmpty(batchMap), "批次机器列表不能为空");
        Preconditions.checkArgument(
                request.getTotalBatchNum().equals(request.isIncludeBastion() ? batchMap.size() - 1 : batchMap.size()),
                "批次总数与列表数不符合");
        Preconditions
                .checkArgument(!request.isIncludeBastion() || batchMap.containsKey(GreyReleaseUtil.BASTION_BATCH_NUM),
                        "包含堡垒机时server列表需指定第0批");
        for (Map.Entry<Integer, List<Host>> batch : batchMap.entrySet()) {
            Preconditions.checkArgument(batch.getKey() <= request.getTotalBatchNum(), "批次编号不能超过总批次数");
            Preconditions.checkArgument(!CollectionUtils.isEmpty(batch.getValue()),
                    String.format("批次%d的机器列表不能为空", batch.getKey()));
        }

        return new TaskConfig(request.getReleaseList(), request.getTotalBatchNum(), request.getBatchInterval(),
                request.isAutoContinue(), request.isIgnorePushFail(), request.isIncludeBastion(), batchMap);
    }

}
