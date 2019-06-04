package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2017 2017/3/15 15:46
 */
public interface DefaultTemplateConfigMappingDao {

    int insert(ConfigMeta meta, long defaultConfigId);

    Long select(ConfigMeta meta);

    int delete(ConfigMeta meta);
}
