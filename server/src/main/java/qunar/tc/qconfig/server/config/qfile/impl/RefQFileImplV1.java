package qunar.tc.qconfig.server.config.qfile.impl;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:51
 */
public class RefQFileImplV1 extends AbstractRefQFile implements QFile {

    public RefQFileImplV1(ConfigMeta source, QFile delegate) {
        super(source, delegate);
    }

    @Override
    public Optional<Changed> checkChange(CheckRequest request, String ip) {
        ConfigMeta delegateMeta = delegate.getSourceMeta();
        request = new CheckRequest(delegateMeta.getGroup(), delegateMeta.getDataId(), delegateMeta.getProfile(), request.getVersion());

        Optional<Changed> changed = delegate.checkChange(request, ip);
        if (changed.isPresent()) {
            ConfigMeta source = getSourceMeta();
            changed = Optional.of(new Changed(source.getGroup(), source.getDataId(), source.getProfile(), changed.get().getNewestVersion()));
        }
        return changed;
    }
}
