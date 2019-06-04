package qunar.tc.qconfig.server.config.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.server.config.ConfigService;
import qunar.tc.qconfig.server.config.check.CheckResult;
import qunar.tc.qconfig.server.config.check.CheckService;
import qunar.tc.qconfig.server.config.check.CheckUtil;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:56
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private static Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    @Resource(name = "cacheConfigInfoService")
    private ConfigInfoService cacheConfigInfoService;

    @Resource(name = "dbConfigInfoService")
    private ConfigInfoService dbConfigInfoService;

    @Resource
    private CheckService checkService;

    @Override
    public Collection<Changed> checkChangedList(QFileFactory qFileFactory, List<CheckRequest> requests, String ip, boolean needPurge) {
        return doCheckChange(qFileFactory, requests, ip, needPurge);
    }

    private Collection<Changed> doCheckChange(QFileFactory qFileFactory, List<CheckRequest> requests, String ip, boolean needPurge) {
        CheckResult checkResult = checkService.check(requests, ip, qFileFactory);
        if (!needPurge || checkResult.getRequestsNoFile().isEmpty()) {
            return CheckUtil.processStringCase(checkResult.getChanges());
        }

        List<Changed> changes = Lists.newArrayListWithCapacity(checkResult.getChanges().size() + checkResult.getRequestsNoFile().size());
        changes.addAll(CheckUtil.processStringCase(checkResult.getChanges()));
        for (CheckRequest requestNoFile : checkResult.getRequestsNoFile()) {
            Changed purgeNotify = new Changed(requestNoFile.getGroup(), requestNoFile.getDataId(), requestNoFile.getLoadProfile(), Constants.PURGE_FILE_VERSION);
            changes.add(purgeNotify);
        }
        return changes;
    }

    @Override
    public Map.Entry<QFile, ChecksumData<String>> findConfig(QFileFactory qFileFactory, VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        Optional<QFile> qFile = qFileFactory.create(configId.getData(), cacheConfigInfoService);
        if (!qFile.isPresent()) {
            logger.warn("findConfig未能从内存缓存中找到配置文件的元信息, meta[{}]", configId.getData());
            throw new ConfigNotFoundException();
        }

        return Maps.immutableEntry(qFile.get(), qFile.get().findConfig(configId.getVersion()));
    }

    @Override
    public Map.Entry<QFile, VersionData<ChecksumData<String>>> forceLoad(QFileFactory qFileFactory, String ip, VersionData<ConfigMeta> configId) throws ConfigNotFoundException {
        Optional<QFile> qFile = forceFindFile(qFileFactory, configId.getData());
        if (!qFile.isPresent()) {
            logger.warn("forceLoad未能找到配置文件的元信息, meta[{}]", configId.getData());
            throw new ConfigNotFoundException();
        }

        return Maps.immutableEntry(qFile.get(), qFile.get().forceLoad(ip, configId.getVersion()));
    }

    private Optional<QFile> forceFindFile(QFileFactory qFileFactory, ConfigMeta meta) {
        Optional<QFile> qFile = qFileFactory.create(meta, cacheConfigInfoService);
        if (!qFile.isPresent()) {
            qFile = qFileFactory.create(meta, dbConfigInfoService);
        }
        return qFile;
    }

}
