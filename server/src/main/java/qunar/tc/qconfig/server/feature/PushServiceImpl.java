package qunar.tc.qconfig.server.feature;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.cache.CacheConfigVersionService;
import qunar.tc.qconfig.server.config.cache.CachePushConfigVersionService;
import qunar.tc.qconfig.server.config.longpolling.LongPollingStore;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;
import qunar.tc.qconfig.servercommon.bean.PushConfigVersionItem;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2017 2017/4/5 19:36
 */
@Service
public class PushServiceImpl implements PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    @Resource
    private CacheConfigVersionService cacheConfigVersionService;

    @Resource
    private CachePushConfigVersionService cachePushConfigVersionService;

    @Resource
    private LongPollingStore longPollingStore;

    @Override
    public void push(ConfigMeta meta, long version, Set<IpAndPort> ipAndPorts) {
        Optional<Long> configVersion = cacheConfigVersionService.getVersion(meta);
        if (!configVersion.isPresent()) {
            logger.warn("config version not find, {}, version {}, {}", meta, version, ipAndPorts);
            return;
        }

        // 更新push config cache，留存push record
        for (IpAndPort ipAndPort : ipAndPorts) {
            cachePushConfigVersionService.update(new PushConfigVersionItem(meta, ipAndPort.getIp(), version));
        }

        longPollingStore.manualPush(meta, version, ipAndPorts);
    }

    @Override
    public void pushWithIp(ConfigMeta meta, long version, Set<String> ips) {
        for (String ip : ips) {
            cachePushConfigVersionService.update(new PushConfigVersionItem(meta, ip, version));
        }
        longPollingStore.manualPushIps(meta, version, ips);
    }
}
