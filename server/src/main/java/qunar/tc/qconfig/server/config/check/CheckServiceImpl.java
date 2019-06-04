package qunar.tc.qconfig.server.config.check;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 17:09
 */
@Service
public class CheckServiceImpl implements CheckService {

    @Resource
    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    @Resource(name = "cacheConfigInfoService")
    private ConfigInfoService cacheConfigInfoService;

    @Override
    public CheckResult check(List<CheckRequest> requests, String ip, QFileFactory qFileFactory) {
        List<CheckRequest> requestsNoFile = Lists.newArrayList();
        Map<CheckRequest, Changed> changes = Maps.newHashMap();
        Map<CheckRequest, QFile> requestNoChange = Maps.newHashMap();
        Map<CheckRequest, QFile> requestsLockByFixVersion = Maps.newHashMap();
        for (CheckRequest request : requests) {
            ConfigMeta meta = new ConfigMeta(request.getGroup(), request.getDataId(), request.getProfile());
            Optional<QFile> qFileOptional = qFileFactory.create(meta, cacheConfigInfoService);
            if (!qFileOptional.isPresent()) {
                requestsNoFile.add(request);
                continue;
            }

            QFile qFile = qFileOptional.get();
            Optional<Changed> changedOptional = qFile.checkChange(request, ip);
            if (changedOptional.isPresent()) {
                Optional<Changed> resultChange = repairChangeWithFixVersion(qFile, request, ip, changedOptional.get());
                if (resultChange.isPresent()) {
                    changes.put(request, resultChange.get());
                } else {
                    requestsLockByFixVersion.put(request, qFile);
                }
            } else {
                requestNoChange.put(request, qFile);
            }
        }
        return new CheckResult(requestsNoFile, changes, requestNoChange, requestsLockByFixVersion);
    }

    private Optional<Changed> repairChangeWithFixVersion(QFile file, CheckRequest request, String ip, Changed changed) {
        Monitor.checkChangeFixedVersionCounter.inc();
        Optional<Long> fixedVersion = cacheFixedVersionConsumerService.getFixedVersion(file.getRealMeta(), ip);
        if (!fixedVersion.isPresent()) {
            return Optional.of(changed);
        } else if (loadFileChanged(request, changed) || fixedVersion.get() > request.getVersion()) {
            return Optional.of(new Changed(changed.getGroup(), changed.getDataId(), changed.getProfile(), fixedVersion.get()));
        } else {
            return Optional.absent();
        }
    }

    private boolean loadFileChanged(CheckRequest request, Changed changed) {
        return !request.getLoadProfile().equals(changed.getProfile());
    }
}
