package qunar.tc.qconfig.server.dao;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/5/15 18:40
 */
public interface FileDeleteDao {

    void delete(ConfigMeta meta, String ip);

    List<ConfigMeta> select(String ip);
}
