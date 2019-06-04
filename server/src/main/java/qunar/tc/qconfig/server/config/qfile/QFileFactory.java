package qunar.tc.qconfig.server.config.qfile;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:43
 */
public interface QFileFactory {

    String getName();

    Optional<QFile> create(ConfigMeta meta, ConfigInfoService configInfoService);

    Optional<QFile> internalCreate(ConfigMeta meta, ConfigMeta candidate, ConfigInfoService configInfoService);
}
