package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.ClientLog;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/9 17:25
 */
public interface ClientLogDao {

    List<ClientLog> selectRecent(String group, String profile, String dataId, long basedVersion);

    List<Long> selectIds(DbEnv env, String endTime, int limit);

    int delete(ConfigMeta meta);

    int delete(DbEnv env, List<Long> ids);
}
