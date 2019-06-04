package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.service.NotifyService;

import java.util.Optional;

/**
 * @author zhenyu.nie created on 2018 2018/5/21 14:48
 */
public class CancelReleaseStatus extends AbstractReleaseStatus {

    private final static Logger logger = LoggerFactory.getLogger(CancelReleaseStatus.class);

    public CancelReleaseStatus(StatusInfo statusInfo, ReleaseStatusFactory factory, NotifyService notifyService) {
        super(statusInfo, factory, notifyService);
    }

    @Override
    protected java.util.Optional<ReleaseStatus> cancel() {
        return java.util.Optional.empty();
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
        return greyReleaseService.cancelTask(statusInfo);
    }
}
