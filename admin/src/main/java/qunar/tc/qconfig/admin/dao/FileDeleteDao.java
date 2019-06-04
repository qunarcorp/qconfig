package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/5/15 18:19
 */
public interface FileDeleteDao {

    void insert(ConfigMeta meta, List<String> ips);

    boolean exist(ConfigMeta meta);
}
