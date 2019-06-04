package qunar.tc.qconfig.admin.service.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.PublishKeyInterceptStrategyDao;
import qunar.tc.qconfig.admin.model.InterceptStrategy;
import qunar.tc.qconfig.admin.service.PublishKeyInterceptStrategyService;
import qunar.tc.qconfig.admin.service.UserContextService;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2017 2017/3/22 20:49
 */
@Service
public class PublishKeyInterceptStrategyServiceImpl implements PublishKeyInterceptStrategyService {

    private static final InterceptStrategy DEFAULT_STRATEGY = InterceptStrategy.NO;

    @Resource
    private PublishKeyInterceptStrategyDao interceptStrategyDao;

    @Resource
    private UserContextService userContext;

    @Override
    public InterceptStrategy getStrategy(String group) {
        InterceptStrategy strategy = interceptStrategyDao.select(group);
        return strategy != null ? strategy : DEFAULT_STRATEGY;
    }

    @Override
    public void setStrategy(String group, InterceptStrategy strategy) {
        interceptStrategyDao.update(group, strategy, userContext.getRtxId());
    }
}
