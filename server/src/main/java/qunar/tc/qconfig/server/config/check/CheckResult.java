package qunar.tc.qconfig.server.config.check;

import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.config.qfile.QFile;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 16:59
 */
public class CheckResult {

    private final List<CheckRequest> requestsNoFile;

    private final Map<CheckRequest, Changed> changes;

    private final Map<CheckRequest, QFile> requestsNoChange;

    private final Map<CheckRequest, QFile> requestsLockByFixVersion;

    public CheckResult(List<CheckRequest> requestsNoFile,
                       Map<CheckRequest, Changed> changes,
                       Map<CheckRequest, QFile> requestsNoChange,
                       Map<CheckRequest, QFile> requestsLockByFixVersion) {
        this.requestsNoFile = requestsNoFile;
        this.changes = changes;
        this.requestsNoChange = requestsNoChange;
        this.requestsLockByFixVersion = requestsLockByFixVersion;
    }

    public List<CheckRequest> getRequestsNoFile() {
        return requestsNoFile;
    }

    public Map<CheckRequest, Changed> getChanges() {
        return changes;
    }

    public Map<CheckRequest, QFile> getRequestsNoChange() {
        return requestsNoChange;
    }

    public Map<CheckRequest, QFile> getRequestsLockByFixVersion() {
        return requestsLockByFixVersion;
    }
}
