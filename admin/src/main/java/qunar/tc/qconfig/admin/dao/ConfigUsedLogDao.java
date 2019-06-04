package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 15:43
 */
public interface ConfigUsedLogDao {

    List<ConfigUsedLog> select(String group, String dataId, String profile);

    List<ConfigUsedLog> select(String group, String dataId, String profile, String ip);

    List<ConfigUsedLog> select(String group, String dataId, String profile, List<ConfigUsedType> configUsedTypes);

    List<ConfigUsedLog> select(String sourceGroupId, String consumerProfile);

    ConfigUsedLog selectNewest(String group, String dataId, String profile, String ip);

    int delete(ConfigMeta configMeta);

    int completeDelete(ConfigMeta meta);
}
