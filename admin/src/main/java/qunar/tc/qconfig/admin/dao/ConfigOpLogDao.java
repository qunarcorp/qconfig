package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 21:59
 */
public interface ConfigOpLogDao {

    List<ConfigOpLog> selectRecent(ConfigMeta configMeta, long basedVersion, int length);

    List<ConfigOpLog> selectRecent(String group, String profile, int length);

    List<ConfigOpLog> selectRecent(String operator, int offset, int length);

    int insert(ConfigOpLog configOpLog);

    int completeDelete(ConfigMeta meta);
}
