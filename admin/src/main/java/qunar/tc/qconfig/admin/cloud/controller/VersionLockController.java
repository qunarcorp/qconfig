package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import qunar.tc.qconfig.admin.cloud.vo.ClientFileVersionRequest;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/qconfig/consumer/version")
public class VersionLockController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private FixedConsumerVersionService fixedConsumerVersionService;

    @RequestMapping(value = "/lock", method = RequestMethod.POST)
    @ResponseBody
    public Object fix(@RequestBody ClientFileVersionRequest clientFileVersion) {
        Long version = clientFileVersion.getVersion();
        String ip = clientFileVersion.getIp().trim();
        logger.info("lock consumer version, {}", clientFileVersion);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ip), "ip不能为空");
        ConfigMeta configMeta = transform(clientFileVersion);
        checkLegalMeta(configMeta);
        checkLegalVersion(version);
        try {
            fixedConsumerVersionService.fixConsumerVersion(configMeta, ip, version);
        } catch (DuplicateKeyException e){
            logger.error("lock consumer version duplicated", e);
            return JsonV2.failOf("不可重复锁定");
        } catch (Exception e) {
            logger.error("lock consumer version error", e);
            return new JsonV2<>(-1, "锁定版本失败，请联系管理员！", null);
        }
        return new JsonV2<>(0, "锁定版本成功", null);
    }

    @RequestMapping(value = "/unlock", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(@RequestBody ClientFileVersionRequest clientFileVersion) {
        String ip = clientFileVersion.getIp().trim();
        logger.info("unlock consumer version, {}", clientFileVersion);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ip), "ip不能为空");
        ConfigMeta configMeta = transform(clientFileVersion);
        checkLegalMeta(configMeta);
        try {
            fixedConsumerVersionService.deleteConsumerVersion(configMeta, ip);
        } catch (Exception e) {
            logger.error("unlock consumer version error", e);
            return new JsonV2<>(-1, "取消锁定失败，请联系管理员！", null);
        }
        return new JsonV2<>(0, "取消锁定成功", null);
    }

}
