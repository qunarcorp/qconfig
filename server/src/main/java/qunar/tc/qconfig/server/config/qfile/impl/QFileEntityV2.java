package qunar.tc.qconfig.server.config.qfile.impl;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.impl.CacheConfigInfoService;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/11/3 16:26
 */
public class QFileEntityV2 extends AbstractQFileEntity implements QFile {

    public QFileEntityV2(ConfigMeta meta,
                         CacheConfigInfoService cacheConfigInfoService,
                         ConfigStore configStore,
                         LogService logService,
                         ClientInfoService clientInfoService) {

        super(meta, cacheConfigInfoService, configStore, logService, clientInfoService);
    }

    @Override
    public Optional<Changed> checkChange(CheckRequest request, String ip) {
        ConfigMeta meta = getSourceMeta();
        Optional<Long> version = getCacheConfigInfoService().getVersion(meta, ip);
        if (!version.isPresent()) {
            return Optional.absent();
        }

        if (meta.getProfile().equalsIgnoreCase(request.getLoadProfile()) && version.get() <= request.getVersion()) {
            return Optional.absent();
        }

        return Optional.of(new Changed(meta.getGroup(), meta.getDataId(), meta.getProfile(), version.get()));
    }
}