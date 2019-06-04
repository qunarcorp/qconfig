package qunar.tc.qconfig.admin.dao;

/**
 * @author zhenyu.nie created on 2016 2016/10/19 21:24
 */
public interface DefaultTemplateConfigDao {

    long insert(String templateConfig);

    String select(long id);
}
