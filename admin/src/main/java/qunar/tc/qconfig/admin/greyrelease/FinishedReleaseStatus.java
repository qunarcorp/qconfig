package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.service.NotifyService;

import java.util.Optional;

/**
 * @author zhenyu.nie created on 2018 2018/5/21 16:19
 */
public class FinishedReleaseStatus extends AbstractReleaseStatus {

    private static final Logger logger = LoggerFactory.getLogger(FinishedReleaseStatus.class);

    public FinishedReleaseStatus(StatusInfo statusInfo, ReleaseStatusFactory factory, NotifyService notifyService) {
        super(statusInfo, factory, notifyService);
    }

    @Override
    protected Optional<ReleaseStatus> pause() {
        return java.util.Optional.empty();
    }

    @Override
    protected java.util.Optional<ReleaseStatus> next() {
        return java.util.Optional.empty();
    }

    @Override
    public ListenableFuture<?> work() {
        return Futures.immediateFuture(null);
    }

    @Override
    public boolean doSave() {
        return greyReleaseService.finishTask(statusInfo);
    }
}
