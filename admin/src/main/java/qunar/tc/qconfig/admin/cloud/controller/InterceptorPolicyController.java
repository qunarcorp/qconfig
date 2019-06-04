package qunar.tc.qconfig.admin.cloud.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.model.InterceptStrategy;
import qunar.tc.qconfig.admin.service.PublishKeyInterceptStrategyService;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.Resource;


@Controller
@RequestMapping("/qconfig/interceptor")
public class InterceptorPolicyController extends AbstractControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(InterceptorPolicyController.class);

    @Resource
    private PublishKeyInterceptStrategyService interceptStrategyService;


    @RequestMapping("/getPublishKeyInterceptStrategy")
    @ResponseBody
    public Object getStrategy(@RequestParam String group) {
        return JsonV2.successOf(interceptStrategyService.getStrategy(group).code());
    }

    @RequestMapping("/setPublishKeyInterceptStrategy")
    @ResponseBody
    public Object setStrategy(@RequestParam String group, @RequestParam(value = "strategy") int strategyCode) {
        logger.info("set publish key intercept strategy, group=[{}], strategy code=[{}]", group, strategyCode);
        try {
            InterceptStrategy strategy = InterceptStrategy.codeOf(strategyCode);
            interceptStrategyService.setStrategy(group, strategy);
            return JsonV2.successOf(true);
        } catch (RuntimeException e) {
            logger.error("set publish key intercept strategy error, group=[{}], strategy code=[{}]", group, strategyCode, e);
            return new JsonV2<>(-1, "服务器异常", null);
        }
    }
}
