package qunar.tc.qconfig.server.config.rest;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.server.config.ConfigTypeService;
import qunar.tc.qconfig.server.config.ConfigService;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.exception.IllegalFileTypeException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/3/27 14:32
 */
@Service
public class RestConfigServiceImpl implements RestConfigService {

    private static final Logger logger = LoggerFactory.getLogger(RestConfigServiceImpl.class);

    @Resource
    private ConfigTypeService configTypeService;

    @Resource
    private ConfigService wrappedConfigService;

    @Resource(name = "cacheConfigInfoService")
    private ConfigInfoService cacheConfigInfoService;

    @Override
    @SuppressWarnings("all")
    public RestFile getRestFile(ConfigMeta meta, Optional<Long> version, QFileFactory factory) throws IllegalFileTypeException, ConfigNotFoundException {
        if (!isRestFile(meta)) {
            throw new IllegalFileTypeException();
        }

        long fileVersion = getRealVersion(meta, version);
        VersionData<ConfigMeta> configId = VersionData.of(fileVersion, meta);
        logger.debug("get config, {}", configId);
        Map.Entry<QFile, ChecksumData<String>> file = wrappedConfigService.findConfig(factory, configId);
        return new RestFile(file.getKey(), fileVersion, file.getValue().getData(), file.getValue().getCheckSum());
    }

    @SuppressWarnings("all")
    private long getRealVersion(ConfigMeta meta, Optional<Long> version) throws ConfigNotFoundException {
        if (version.isPresent()) {
            return version.get();
        }

        version = cacheConfigInfoService.getVersion(meta);
        if (version.isPresent()) {
            return version.get();
        } else {
            throw new ConfigNotFoundException();
        }
    }

    private boolean isRestFile(ConfigMeta meta) {
        return FileChecker.isJsonFile(meta.getDataId()) && configTypeService.isRest(meta.getGroup(), meta.getDataId());
    }
}
