package qunar.tc.qconfig.admin.web.api;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ReferenceService;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2018 2018/2/1 15:03
 */
@Controller
@RequestMapping("api")
public class ReferenceSearchController {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceSearchController.class);

    private static final int ARGUMENT_ERROR = 1;

    private static final int SYSTEM_ERROR = -1;

    private static final JsonV2<Void> ARGUMENT_ERROR_RETURN = new JsonV2<>(ARGUMENT_ERROR, "appCode不能为空", null);

    private static final JsonV2<Void> SYSTEM_ERROR_RETURN = new JsonV2<>(SYSTEM_ERROR, "system error", null);

    @Resource
    private ReferenceService referenceService;

    @RequestMapping("reference/relative")
    @ResponseBody
    public Object searchRelative(@RequestParam(required = false) String tenant,
                                 @RequestParam(required = false) String appCode) {
        if (Strings.isNullOrEmpty(appCode)) {
            return ARGUMENT_ERROR_RETURN;
        }

        try {

            return JsonV2.successOf(referenceService.searchRelative(appCode));
        } catch (Throwable e) {
            logger.error("reference relative search error, tenant [{}], appCode [{}]", tenant, appCode, e);
            Monitor.referenceRelativeSearchErrorCounter.inc();
            return SYSTEM_ERROR_RETURN;
        }
    }
}
