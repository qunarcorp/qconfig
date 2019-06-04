package qunar.tc.qconfig.admin.web.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.admin.web.security.Account;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.Resource;

@Controller
@RequestMapping("/api/profile")
public class ApiProfileController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(ApiProfileController.class);

    @Resource
    private ProfileService profileService;

    @Resource
    private UserContextService userContext;


    @RequestMapping("/exists")
    @ResponseBody
    public Object exists(@RequestParam String group, @RequestParam String profile) {
        CheckUtil.checkLegalGroup(group);
        CheckUtil.checkLegalProfile(profile);
        return profileService.exist(group, profile);
    }

    @RequestMapping("/create")
    @ResponseBody
    public Object createBuildGroup(@RequestParam String group, @RequestParam String profile) {
        CheckUtil.checkLegalGroup(group);
        CheckUtil.checkLegalProfile(profile);
        try {
            profileService.create(group, profile);
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("create buildGroup failed! group[{}], profile:[{}]", group, profile, e);
            return JsonV2.failOf("创建buildGroup失败");
        }
    }

    // TODO: 2019-05-15 z这里
    @RequestMapping("/delete")
    @ResponseBody
    public Object deleteProfile(@RequestParam String group, @RequestParam String profile) {
        userContext.setAccount(new Account("api"));
        userContext.freshGroupInfos();
        try {
            return false;
        } finally {
            userContext.clear();
        }
    }
}
