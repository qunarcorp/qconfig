package qunar.tc.qconfig.admin.web.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.dao.FilePublicStatusDao;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.PublicType;

import javax.annotation.Resource;

@Controller
@RequestMapping("/qconfig/api/file")
public class ApiFileController {

    private final Logger logger = LoggerFactory.getLogger(ApiFileController.class);

    @Resource
    private ConfigService configService;

    @Resource
    private FilePublicStatusDao filePublicStatusDao;


    @RequestMapping("/list")
    @ResponseBody
    public Object getProfileInfo(@RequestParam("group") String group, @RequestParam("profile") String profile) {
        CheckUtil.checkLegalGroup(group);
        CheckUtil.checkLegalProfile(profile);

        try {
            return JsonV2.successOf(configService.getPublishedConfig(group, profile));
        } catch (RuntimeException e) {
            logger.error("get profile info error, group=[{}], profile=[{}]", group, profile, e);
            throw e;
        }
    }

    @RequestMapping("/listPublicGroup")
    @ResponseBody
    public Object getPublicGroup(){
        try {
            return JsonV2.successOf(filePublicStatusDao.findAllPublicGroup(PublicType.PUBLIC_MASK));
        } catch (RuntimeException e){
            logger.error("get all public file error");
            throw e;
        }
    }
}
