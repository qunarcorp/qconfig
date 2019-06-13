package qunar.tc.qconfig.admin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 22:22
 */
@Controller
public class BuildGroupController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ProfileService profileService;

    @RequestMapping(value = "/edit/createBuildGroup", method = RequestMethod.POST)
    @ResponseBody
    public Object createBuildGroup(@RequestParam("group") String group, @RequestParam("profile") String profile) {
        logger.info("create profile, group=[{}], profile=[{}]", group, profile);

        checkLegalGroup(group);
        checkLegalProfile(profile);

        String buildGroup = ProfileUtil.getBuildGroup(profile);
        checkArgument(buildGroup.length() > 0 && buildGroup.length() <= Environment.BUILD_GROUP_MAX_LENGTH,
                QConfigAttributesLoader.getInstance().getBuildGroup() + "长度必须在1到" + Environment.BUILD_GROUP_MAX_LENGTH + "之间");
        checkArgument(ProfileUtil.BUILD_GROUP_LETTER_DIGIT_PATTERN.matcher(buildGroup).find(),
                QConfigAttributesLoader.getInstance().getBuildGroup() + "不能包含[字符，数字，'_'，'-']以外的其它内容");

        try {
            profileService.create(group, profile);
            return true;
        } catch (ModifiedException e) {
            logger.info("{} has been created before, group=[{}], profile=[{}]", QConfigAttributesLoader.getInstance().getBuildGroup(), group, profile);
            throw new RuntimeException(QConfigAttributesLoader.getInstance().getBuildGroup() + "已经存在");
        } catch (Exception e) {
            logger.error("create {} error, group=[{}], profile=[{}]", QConfigAttributesLoader.getInstance().getBuildGroup(), group, profile, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }
}
