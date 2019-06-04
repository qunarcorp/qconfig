package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 12:08
 */
public interface PushConfigVersionDao {

    List<Map.Entry<Long, Long>> selectIdAndVersions(ConfigMeta meta, long maxVersion);

    void delete(List<Map.Entry<Long, Long>> idVersions);



    void update(ConfigMeta meta, List<String> ips, long version);
}
