package qunar.tc.qconfig.server.config.qfile.impl;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:45
 */
public abstract class AbstractDelegatedQFile extends AbstractQFile implements QFile {

    protected QFile delegate;

    protected AbstractDelegatedQFile(QFile delegate) {
        this.delegate = delegate;
    }

    @Override
    public abstract ConfigMeta getSourceMeta();

    @Override
    public ConfigMeta getRealMeta() {
        return delegate.getRealMeta();
    }

    @Override
    public Optional<Changed> checkChange(CheckRequest request, String ip) {
        return delegate.checkChange(request, ip);
    }

    @Override
    public ChecksumData<String> findConfig(long version) throws ConfigNotFoundException {
        return delegate.findConfig(version);
    }

    @Override
    public VersionData<ChecksumData<String>> forceLoad(String ip, long version) throws ConfigNotFoundException {
        return delegate.forceLoad(ip, version);
    }

    @Override
    public LogService getLogService() {
        return delegate.getLogService();
    }
}
