package qunar.tc.qconfig.server.config.qfile.impl;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/11/3 16:56
 */
public class RefQFileImplV2 extends AbstractRefQFile implements QFile {

    public RefQFileImplV2(ConfigMeta source, QFile delegate) {
        super(source, delegate);
    }

    @Override
    public Optional<Changed> checkChange(CheckRequest request, String ip) {
        ConfigMeta meta = getSourceMeta();
        ConfigMeta delegateMeta = delegate.getSourceMeta();
        if (meta.getProfile().equalsIgnoreCase(request.getLoadProfile())) {
            request = new CheckRequest(delegateMeta.getGroup(), delegateMeta.getDataId(), delegateMeta.getProfile(), delegateMeta.getProfile(), request.getVersion());
        } else {
            // 这里将请求的loadProfile设为other，这意味着被引用的文件在进行判断时，checkChange肯定不会为空（除了本身已不存在的情况）
            request = new CheckRequest(delegateMeta.getGroup(), delegateMeta.getDataId(), delegateMeta.getProfile(), "other", request.getVersion());
        }

        Optional<Changed> changed = delegate.checkChange(request, ip);
        if (changed.isPresent()) {
            changed = Optional.of(new Changed(meta.getGroup(), meta.getDataId(), meta.getProfile(), changed.get().getNewestVersion()));
        }
        return changed;
    }
}