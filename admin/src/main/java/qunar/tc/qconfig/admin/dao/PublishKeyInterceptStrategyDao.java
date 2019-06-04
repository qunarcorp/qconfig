package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.InterceptStrategy;

/**
 * @author zhenyu.nie created on 2017 2017/3/22 20:38
 */
public interface PublishKeyInterceptStrategyDao {

    InterceptStrategy select(String group);

    int update(String group, InterceptStrategy strategy, String operator);
}
