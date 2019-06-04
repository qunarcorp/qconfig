package qunar.tc.qconfig.server.config.qfile.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.config.impl.CacheConfigInfoService;
import qunar.tc.qconfig.server.config.impl.DbConfigInfoService;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:10
 */
@Service("v2Factory")
public class QFileFactoryV2Impl extends AbstractQFileFactory implements QFileFactory {

    @Resource
    private ConfigStore configStore;

    @Resource
    private LogService logService;

    @Resource
    private ClientInfoService clientInfoService;

    @Resource
    private CacheConfigInfoService cacheConfigInfoService;

    @Resource
    private DbConfigInfoService dbConfigInfoService;

    @Resource
    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    @Override
    public String getName() {
        return "qFileV2Factory";
    }

    @Override
    protected QFile createFile(ConfigMeta meta) {
        return new QFileEntityV2(meta, cacheConfigInfoService, configStore, logService, clientInfoService);
    }

    @Override
    protected QFile createShareFile(ConfigMeta meta, QFile sharedFile) {
        return new ShareQFileV2(meta, sharedFile);
    }

    @Override
    protected QFile createRefFile(ConfigMeta meta, QFile referencedFile) {
        return new RefQFileImplV2(meta, referencedFile);
    }

    @Override
    protected QFile createInheritFile(ConfigMeta meta, QFile childFile, QFile inheritedFile) {
        return new InheritQFileV2(meta, childFile, inheritedFile, configStore, logService, clientInfoService, cacheConfigInfoService, dbConfigInfoService, cacheFixedVersionConsumerService);
    }
}
