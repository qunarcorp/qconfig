package qunar.tc.qconfig.server.config.rest;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.exception.IllegalFileTypeException;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/3/27 14:28
 */
public interface RestConfigService {

    @SuppressWarnings("all")
    RestFile getRestFile(ConfigMeta meta, Optional<Long> version, QFileFactory factory) throws IllegalFileTypeException, ConfigNotFoundException;
}
