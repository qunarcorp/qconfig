package qunar.tc.qconfig.server.config.qfile.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.config.impl.CacheConfigInfoService;
import qunar.tc.qconfig.server.config.impl.DbConfigInfoService;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:45
 */
@Service("v1Factory")
public class QFileFactoryV1Impl extends AbstractQFileFactory implements QFileFactory {

    @Resource
    private ConfigStore configStore;

    @Resource
    private LogService logService;

    @Resource
    private CacheConfigInfoService cacheConfigInfoService;

    @Resource
    private DbConfigInfoService dbConfigInfoService;

    @Resource
    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    @Override
    public String getName() {
        return "qFileV1Factory";
    }

    @Override
    protected QFile createFile(ConfigMeta meta) {
        return new QFileEntityV1(meta, cacheConfigInfoService, configStore, logService, getClientInfoService());
    }

    @Override
    protected QFile createShareFile(ConfigMeta meta, QFile sharedFile) {
        return new ShareQFileV1(meta, sharedFile);
    }

    @Override
    protected QFile createRefFile(ConfigMeta meta, QFile referencedFile) {
        return new RefQFileImplV1(meta, referencedFile);
    }

    @Override
    protected QFile createInheritFile(ConfigMeta meta, QFile childFild, QFile inheritedFile) {
        return new InheritQFileV2(meta, childFild, inheritedFile, configStore, logService, getClientInfoService(), cacheConfigInfoService, dbConfigInfoService, cacheFixedVersionConsumerService);
    }
}
