package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.GreyReleaseService;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.admin.service.PushConfigVersionService;
import qunar.tc.qconfig.admin.service.PushHistoryService;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;

import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * @author zhenyu.nie created on 2018 2018/5/21 14:28
 */
public abstract class AbstractReleaseStatus implements ReleaseStatus {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final int DELAY_TIME_MS = 2000;

    protected static final ListeningScheduledExecutorService executor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(20, new NamedThreadFactory("grey-release")));

    protected final StatusInfo statusInfo;

    protected final ReleaseStatusFactory factory;

    protected GreyReleaseService greyReleaseService;

    protected NotifyService notifyService;

    protected PushConfigVersionService pushConfigVersionService;

    protected ConfigService configService;

    protected PushHistoryService pushHistoryService;

    public void setPushHistoryService(PushHistoryService pushHistoryService) {
        this.pushHistoryService = pushHistoryService;
    }

    public AbstractReleaseStatus(StatusInfo statusInfo, ReleaseStatusFactory factory, NotifyService notifyService) {
        this.statusInfo = statusInfo;
        this.factory = factory;
        this.notifyService = notifyService;
    }

    void setGreyReleaseService(GreyReleaseService greyReleaseService) {
        this.greyReleaseService = greyReleaseService;
    }

    void setPushConfigVersionService(PushConfigVersionService pushConfigVersionService) {
        this.pushConfigVersionService = pushConfigVersionService;
    }

    void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public StatusInfo getStatusInfo() {
        return statusInfo;
    }

    protected Optional<ReleaseStatus> cancel() {
        return Optional.of(factory.create(statusInfo.setState(GreyReleaseState.CANCEL)));
    }

    @Override
    public final Optional<ReleaseStatus> accept(Command command) {
        Optional<ReleaseStatus> nextOptional = translate(command);
        if (nextOptional.isPresent()) {
            ReleaseStatus next = nextOptional.get();
            if (next.save()) {
                next.work();
                return Optional.of(next);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of((ReleaseStatus) this);
        }
    }

    @Override
    public void recover() {
        work();
    }

    protected Optional<ReleaseStatus> translate(Command command) {
        switch (command) {
            case cancel:
                return cancel();
            case pause:
                return pause();
            case next:
                return next();
            default:
                throw new IllegalStateException("unknown command");
        }
    }

    protected abstract Optional<ReleaseStatus> pause();

    protected abstract Optional<ReleaseStatus> next();

    protected abstract boolean doSave();

    @Override
    public boolean save() {
        logger.info("grey save status start, {}", statusInfo);
        boolean success;
        try {
            success = doSave();
        } catch (Throwable e) {
            logger.error("grey save status error, {}", statusInfo);
            throw e;
        }
        logger.info("grey save status finish, result [{}], {}", success, statusInfo);
        return success;
    }


}
