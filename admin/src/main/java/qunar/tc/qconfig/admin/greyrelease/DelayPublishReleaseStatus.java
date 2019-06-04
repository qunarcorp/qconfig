package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zhenyu.nie created on 2018 2018/5/23 13:08
 */
public class DelayPublishReleaseStatus extends AbstractReleaseStatus {

    private final static Logger LOGGER = LoggerFactory.getLogger(DelayPublishReleaseStatus.class);

    private ConfigUsedLogDao configUsedLogDao;

    private ListeningClientsService listeningClientsService;

    public DelayPublishReleaseStatus(StatusInfo statusInfo, ReleaseStatusFactory factory, NotifyService notifyService, ConfigUsedLogDao configUsedLogDao,ListeningClientsService listeningClientsService) {
        super(statusInfo, factory, notifyService);
        this.configUsedLogDao = configUsedLogDao;
        this.listeningClientsService = listeningClientsService;
    }

    @Override
    public ListenableFuture<?> work() {
        long delayMs = computeDelayTime();
        if (delayMs <= 0) {
            return workNow();
        } else {
            return executor.schedule(new Runnable() {
                @Override
                public void run() {
                    accept(Command.next);
                }
            }, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    private ListenableFuture<?> workNow() {
        return executor.submit(new Runnable() {
            @Override
            public void run() {
                accept(Command.next);
            }
        });
    }

    @Override
    public void recover() {
        long delayMs = computeDelayTime();
        if (delayMs <= 0) {
            workNow();
        }
    }

    private long computeDelayTime() {
        long autoContinueMs = statusInfo.getAutoContinueMinutes() * 60L * 1000L;
        long elapsed = System.currentTimeMillis() - statusInfo.getLastPushTime().getTime();
        return autoContinueMs - elapsed;
    }

    @Override
    protected Optional<ReleaseStatus> pause() {
        return java.util.Optional.of(factory.create(statusInfo.copyAndIncLockVersion().setState(GreyReleaseState.WAIT_PUBLISH)));
    }

    @Override
    protected java.util.Optional<ReleaseStatus> next() {
        if (statusInfo.isIgnorePushFail() || allPushSuccess()) {
            return java.util.Optional.of(factory.create(statusInfo.copyAndIncLockVersion().setState(GreyReleaseState.PUBLISHING)));
        } else {
            return pause();
        }
    }

    @Override
    public boolean doSave() {
        return greyReleaseService.updateTask(statusInfo);
    }

    private boolean allPushSuccess() {
        for (ConfigMetaVersion metaVersion : statusInfo.getTaskConfig().getMetaVersions()) {
            List<Host> hosts = statusInfo.getBatches().get(statusInfo.getFinishedBatchNum());
            Set<String> pushFailIps = Sets.newHashSetWithExpectedSize(hosts.size());
            for (Host host : hosts) {
                pushFailIps.add(host.getIp());
            }

            ConfigMeta meta = metaVersion.getConfigMeta();
            try {
                ListenableFuture<Set<ClientData>> clientsDataFuture = listeningClientsService.getListeningClientsData(meta, true);
                Set<ClientData> clientDataSet = clientsDataFuture.get(Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                for (ClientData clientData : clientDataSet) {
                    if (clientData.getVersion() == metaVersion.getVersion()) {
                        pushFailIps.remove(clientData.getIp());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("get client data set error.", e);
            }

            if (!pushFailIps.isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
