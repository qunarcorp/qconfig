package qunar.tc.qconfig.admin.web.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
@Controller
public class FixedConsumerVersionController extends AbstractController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private FixedConsumerVersionService fixedConsumerVersionService;

    @RequestMapping(value = "/version/fix", method = RequestMethod.POST)
    @ResponseBody
    public Object fix(@RequestParam("group") String group,
                      @RequestParam("dataId") String dataId,
                      @RequestParam("profile") String profile,
                      @RequestParam("ip") String ip,
                      @RequestParam("version") String version) {
        ip = ip.trim();
        logger.info("fix consumer version, group={}, dataId={}, profile={}, ip={}, version={}",
                group, dataId, profile, ip, version);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ip), "ip不能为空");
        ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(configMeta);
        try {
            fixedConsumerVersionService.fixConsumerVersion(configMeta, ip, Long.parseLong(version));
        } catch (Exception e) {
            logger.error("fix consumer version error", e);
            return new JsonV2<>(-1, e.getMessage(), null);
        }
        return new JsonV2<>(0, "锁定版本成功", null);
    }

    @RequestMapping(value = "/version/delete", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(@RequestParam("group") String group,
                         @RequestParam("dataId") String dataId,
                         @RequestParam("profile") String profile,
                         @RequestParam("ip") String ip) {
        ip = ip.trim();
        logger.info("delete fixed consumer version, group={}, dataId={}, profile={}, ip={}",
                group, dataId, profile, ip);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ip), "ip不能为空");
        ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(configMeta);
        try {
            fixedConsumerVersionService.deleteConsumerVersion(configMeta, ip);
        } catch (Exception e) {
            logger.error("delete consumer version error", e);
            return new JsonV2<>(-1, "服务器异常", null);
        }
        return new JsonV2<>(0, "取消锁定成功", null);
    }
}
