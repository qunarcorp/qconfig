package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.service.ConsumerService;
import qunar.tc.qconfig.admin.service.FilePublicStatusService;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zhenyu.nie created on 2014 2014/7/17 17:14
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private FilePublicStatusService filePublicStatusService;

    private static final int DEFAULT_PUSH_MAXSIZE = 200;

    @PostConstruct
    public void after() {
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(conf -> {
            String maxsizeStr = conf.get("push.maxsize");
            int maxsize = Numbers.toInt(maxsizeStr, DEFAULT_PUSH_MAXSIZE);
            if (maxsize < 0) {
                maxsize = DEFAULT_PUSH_MAXSIZE;
            }
            logger.info("receive configured push max size is [{}], result max size is [{}]", maxsizeStr, maxsize);
        });
    }

    @Override
    public List<ConfigUsedLog> getConsumerLogs(ConfigMeta meta, List<ConfigUsedType> types) {
        List<ConfigUsedLog> addresses = configUsedLogDao.select(meta.getGroup(), meta.getDataId(), meta.getProfile(), types);

        boolean isPublic = filePublicStatusService.isPublic(new ConfigMetaWithoutProfile(meta));
        if (isPublic) {
            return addresses;
        } else {
            return addresses.stream().filter(IS_NOT_REFERENCE_PREDICATE).collect(Collectors.toList());
        }
    }

    @Override
    public Map<String, List<ConfigUsedLog>> getAppConsumerLogs(String group, String env) {
        List<ConfigUsedLog> configUsedLogs = configUsedLogDao.select(group, env);
        Map<String, List<ConfigUsedLog>> host2ConfigUsedLogs = Maps.newHashMap();
        for (ConfigUsedLog configUsedLog : configUsedLogs) {
            String hostname = configUsedLog.getHostname();
            List<ConfigUsedLog> logs = host2ConfigUsedLogs.get(hostname);
            if (logs == null) {
                logs = Lists.newArrayList();
                logs.add(configUsedLog);
                host2ConfigUsedLogs.put(hostname, logs);
            } else {
                logs.add(configUsedLog);
            }
        }
        return host2ConfigUsedLogs;
    }

    private static final Predicate<ConfigUsedLog> IS_NOT_REFERENCE_PREDICATE = input -> {
        if (input != null) {
            return input.getGroup().equals(input.getSourceGroupId())
                    && input.getDataId().equals(input.getSourceDataId())
                    && input.getProfile().equals(input.getSourceProfile());
        } else {
            return false;
        }
    };
}
