package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.service.NotifyService;

import java.util.Optional;

/**
 * @author zhenyu.nie created on 2018 2018/5/23 13:09
 */
public class WaitPublishReleaseStatus extends AbstractReleaseStatus {

    private final static Logger LOGGER = LoggerFactory.getLogger(WaitPublishReleaseStatus.class);

    public WaitPublishReleaseStatus(StatusInfo statusInfo, ReleaseStatusFactory factory, NotifyService notifyService) {
        super(statusInfo, factory, notifyService);
    }

    @Override
    public ListenableFuture<?> work() {
        return Futures.immediateFuture(null);
    }

    @Override
    protected Optional<ReleaseStatus> pause() {
        return java.util.Optional.empty();
    }

    @Override
    protected java.util.Optional<ReleaseStatus> next() {
        return java.util.Optional.of(factory.create(statusInfo.copyAndIncLockVersion().setState(GreyReleaseState.PUBLISHING)));
    }

    @Override
    public boolean doSave() {
        return greyReleaseService.updateTask(statusInfo);
    }
}
