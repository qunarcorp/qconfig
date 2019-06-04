package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.InterceptStrategy;

/**
 * @author zhenyu.nie created on 2017 2017/3/22 20:48
 */
public interface PublishKeyInterceptStrategyService {

    InterceptStrategy getStrategy(String group);

    void setStrategy(String group, InterceptStrategy strategy);
}
