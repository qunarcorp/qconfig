package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import qunar.tc.qconfig.admin.service.SubenvService;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.Resource;

/**
 * Created by chenjk on 2018/6/8.
 */
@Controller
@RequestMapping("/qconfig")
public class SubenvController extends AbstractControllerHelper {

    private final static Logger logger = LoggerFactory.getLogger(SubenvController.class);

    @Resource
    private SubenvService subenvService;

    @RequestMapping(value = "/subenvs/{appId}/{env}", method = RequestMethod.GET)
    @ResponseBody
    public Object getGroupIdNameMap(@PathVariable("appId") String appId, @PathVariable("env") String env) {
        checkLegalGroup(appId);
        checkLegalProfile(env + ":");
        try {
            return JsonV2.successOf(subenvService.getGroupInfoMapByAppIdAndEnv(appId, env));
        } catch (Exception e) {
            logger.error("get buildGroup info error, appId[{}], env[{}]", appId, env, e);
            return JsonV2.successOf(e.getMessage(), ImmutableMap.of());
        }
    }
}
