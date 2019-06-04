package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 16:44
 */
public interface FileValidateUrlDao {

    int update(ConfigMeta meta, String url, String operator);

    String select(ConfigMeta meta);

    void delete(ConfigMeta meta);
}
