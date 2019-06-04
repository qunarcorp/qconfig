package qunar.tc.qconfig.admin.task;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseState;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatus;
import qunar.tc.qconfig.admin.greyrelease.ReleaseStatusFactory;
import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.admin.service.GreyReleaseService;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.client.spring.QConfig;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/5/22 20:43
 */
@Component
public class GreyReleaseRecoverTask {

    @Resource
    private GreyReleaseService greyReleaseService;

    @Resource
    private ReleaseStatusFactory factory;

    @QConfig("config.properties")
    private Map<String, String> config;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // TODO: 2019-05-13 这里的时间修改为配置
        executor.scheduleWithFixedDelay(new GreyReleaseRecoverTaskRunable(), 5, 5, TimeUnit.MINUTES);
    }

    private class GreyReleaseRecoverTaskRunable implements Runnable {

        @Override
        public void run() {
            List<ReleaseStatus> toRecovers = findToRecovers();
            for (ReleaseStatus toRecover : toRecovers) {
                toRecover.recover();
            }
        }
    }

    private List<ReleaseStatus> findToRecovers() {
        // 任务未更新超时时间, 默认60秒
        int timeout = Numbers.toInt(config.get("greyRelease.recover.taskNotOperatedTimeout"), 60);
        List<StatusInfo> statusInfos = greyReleaseService.queryTasksNotOperatedForSeconds(timeout);
        List<ReleaseStatus> toRecovers = Lists.newArrayListWithCapacity(statusInfos.size());
        for (StatusInfo statusInfo : statusInfos) {
            if (needRecover(statusInfo, timeout)) {
                toRecovers.add(factory.create(statusInfo));
            }
        }
        return toRecovers;
    }

    private boolean needRecover(StatusInfo statusInfo, int timeout) {
        if (statusInfo.getState() == GreyReleaseState.WAIT_PUBLISH) {
            return false;
        }
        if (statusInfo.isAutoContinue()) {
            long nextPushTime = statusInfo.getLastPushTime().getTime() + statusInfo.getTaskConfig().getBatchInterval() * 60 * 1000;
            return nextPushTime + timeout * 1000 >= System.currentTimeMillis();
        }
        return true;
    }

}
