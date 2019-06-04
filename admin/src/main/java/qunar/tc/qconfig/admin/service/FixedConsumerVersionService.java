package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
public interface FixedConsumerVersionService {
    Map<String, Long> findIpAndVersions(ConfigMeta meta);

    List<ConfigUsedLog> addFixedVersion(ConfigMeta configMeta, List<ConfigUsedLog> configUsedLogs);

    void fixConsumerVersion(ConfigMeta configMeta, String ip, long version) throws InterruptedException, ExecutionException, TimeoutException;

    void deleteConsumerVersion(ConfigMeta configMeta, String ip);
}
