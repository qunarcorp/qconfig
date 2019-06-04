package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2017 2017/6/2 16:25
 */
public interface PropertiesTemplateDao {

    String select(ConfigMeta meta);

    int update(ConfigMeta meta, String template, String operator);
}
