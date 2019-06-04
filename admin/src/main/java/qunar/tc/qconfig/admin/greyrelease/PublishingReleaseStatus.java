package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/5/21 16:14
 */
public class PublishingReleaseStatus extends AbstractReleaseStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingReleaseStatus.class);

    public PublishingReleaseStatus(StatusInfo statusInfo, ReleaseStatusFactory factory, NotifyService notifyService) {
        super(statusInfo, factory, notifyService);
    }

    @Override
    public ListenableFuture<?> work() {
        int publishingBatchNum = statusInfo.getFinishedBatchNum() + 1;
        final List<Host> machines = statusInfo.getBatches().get(publishingBatchNum);
        preparePush(machines);
        ListenableFuture<?> f1 = executor.submit(new Runnable() {
            @Override
            public void run() {
                push(machines);
            }
        });

        ListenableScheduledFuture<?> f2 = executor.schedule(new Runnable() {
            @Override
            public void run() {
                push(machines);
            }
        }, DELAY_TIME_MS, TimeUnit.MILLISECONDS);

        ListenableFuture<List<Object>> workFuture = Futures.successfulAsList(f1, f2);
        workFuture.addListener(new Runnable() {
            @Override
            public void run() {
                accept(Command.next);
            }
        }, Constants.CURRENT_EXECUTOR);
        return workFuture;
    }

    @Override
    protected Optional<ReleaseStatus> pause() {
        return java.util.Optional.of(factory.create(statusInfo.copyAndIncLockVersion().setState(GreyReleaseState.WAIT_PUBLISH)));
    }

    @Override
    protected java.util.Optional<ReleaseStatus> next() {
        StatusInfo nextStatus = statusInfo.copyAndIncLockVersion().incrementFinishedBatchNum().setPublishTime(new Date());
        if (isBastionBatch(statusInfo)) {
            nextStatus.setState(GreyReleaseState.WAIT_PUBLISH);
        } else if (isLastBatch(statusInfo)) {
            nextStatus.setState(GreyReleaseState.FINISH);
        } else if (statusInfo.isAutoContinue()) {
            nextStatus.setState(GreyReleaseState.DELAY_PUBLISH);
        } else {
            nextStatus.setState(GreyReleaseState.WAIT_PUBLISH);
        }
        return java.util.Optional.of(factory.create(nextStatus));
    }

    private boolean isBastionBatch(StatusInfo statusInfo) {
        return statusInfo.getFinishedBatchNum() == GreyReleaseUtil.BASTION_BATCH_NUM - 1;
    }

    private boolean isLastBatch(StatusInfo statusInfo) {
        return statusInfo.getFinishedBatchNum() == statusInfo.getTotalBatchNum() - 1;
    }

    private void preparePush(List<Host> machines) {
        if (machines.isEmpty()) {
            return;
        }

        for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {

            ConfigMeta meta = metaVersion.getConfigMeta();
            if (!configService.saveSnapshot(meta, metaVersion.getVersion())) {
                LOGGER.error("status info no snapshot, {}", statusInfo);
                throw new IllegalStateException("status info no snapshot, " + statusInfo);
            }

            pushConfigVersionService.update(meta, machines, metaVersion.getVersion());
            pushHistoryService.addGreyReleaseHistory(meta, metaVersion.getVersion(), machines);
        }
    }

    protected void push(List<Host> machines) {
        if (machines.isEmpty()) {
            return;
        }
        for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {
            notifyService.notifyPushIp(metaVersion.getConfigMeta(), metaVersion.getVersion(), machines);
        }
    }

    @Override
    public boolean doSave() {
        return greyReleaseService.updateTask(statusInfo);
    }
}
