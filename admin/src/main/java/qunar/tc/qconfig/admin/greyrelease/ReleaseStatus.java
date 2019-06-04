package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;

/**
 * @author zhenyu.nie created on 2018 2018/5/21 14:27
 */
public interface ReleaseStatus {

    Optional<ReleaseStatus> accept(Command command);

    ListenableFuture<?> work();

    void recover();

    StatusInfo getStatusInfo();

    boolean save();
}
