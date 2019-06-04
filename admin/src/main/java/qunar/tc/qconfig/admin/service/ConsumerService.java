package qunar.tc.qconfig.admin.service;

import com.google.common.collect.ImmutableList;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/7/17 17:13
 */
public interface ConsumerService {

    List<ConfigUsedLog> getConsumerLogs(ConfigMeta meta, List<ConfigUsedType> types);

    Map<String, List<ConfigUsedLog>> getAppConsumerLogs(String group, String env);

    List<ConfigUsedType> ALLOW_PUSH_TYPE = ImmutableList.of(ConfigUsedType.NO_USE, ConfigUsedType.USE_REMOTE);
}
