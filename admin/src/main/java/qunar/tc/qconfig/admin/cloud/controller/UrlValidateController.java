package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.FileValidateUrlService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;


@Controller
@RequestMapping("/qconfig/validateUrl")
public class UrlValidateController extends AbstractControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(UrlValidateController.class);

    @Resource
    private FileValidateUrlService fileValidateUrlService;

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @RequestMapping("/view")    //"/view/validateUrl"
    @ResponseBody
    public Object viewValidateUrl(@RequestParam String group,
                                        @RequestParam String profile,
                                        @RequestParam String dataId) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        if (!FileChecker.isTemplateFile(dataId)) {
            throw new RuntimeException("not template file");
        }
//        ModelAndView modelAndView = new ModelAndView("/validate/validateUrl");
        Map<String, Object> resultMap = Maps.newHashMap();
        try {
            resultMap.put("group", group);
            resultMap.put("dataId", dataId);
            resultMap.put("profile", profile);
            resultMap.put("url", fileValidateUrlService.getUrl(meta));
            resultMap.put("canOperate",
                    permissionService.hasFilePermission(group, profile, dataId, PermissionType.EDIT)
                            || permissionService.hasFilePermission(group, profile, dataId, PermissionType.APPROVE)
                            || permissionService.hasFilePermission(group, profile, dataId, PermissionType.PUBLISH));
            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("view validate url error, group=[{}], profile=[{}], dataId=[{}]", group, profile, dataId, e);
            throw e;
        }
    }

    @RequestMapping("update") // "/operate/validateUrl"
    @ResponseBody
    public Object setValidateUrl(@RequestBody ValidateInfo validateInfo) {
        logger.info("set validate url, {}", validateInfo);
        Monitor.SET_FILE_VALIDATE_URL.inc();
        try {
            ConfigMeta meta = new ConfigMeta(validateInfo.getGroup(), validateInfo.getDataId(), validateInfo.getProfile());
            checkLegalMeta(meta);
            String url = Strings.nullToEmpty(validateInfo.getUrl()).trim();
            checkLegalUrl(url);
            fileValidateUrlService.setUrl(meta, url);
            return JsonV2.successOf(true);
        } catch (IllegalArgumentException e) {
            return new JsonV2<>(-1, e.getMessage(), null);
        } catch (RuntimeException e) {
            logger.error("set validate url error, {}", validateInfo, e);
            return new JsonV2<>(1, "set validate url error", null);
        }
    }

    @RequestMapping("/check") //"/view/checkValidateUrl"
    @ResponseBody
    public Object checkValidateUrl(@RequestParam String validateUrl) {
        try {
            checkLegalUrl(validateUrl);
            return JsonV2.successOf(true);
        } catch (IllegalArgumentException e) {
            return new JsonV2<>(-1, e.getMessage(), null);
        }
    }

    private void checkLegalUrl(String url) {
        if (Strings.isNullOrEmpty(url)) {
            return;
        }
        if (!url.startsWith("http://")) {
            throw new IllegalArgumentException("url must start with http://");
        }
        if (url.length() > 255) {
            throw new IllegalArgumentException("url too long");
        }
    }

    private static class ValidateInfo {
        private String group;
        private String dataId;
        private String profile;
        private String url;

        public String getGroup() {
            return group;
        }

        public String getDataId() {
            return dataId;
        }

        public String getProfile() {
            return profile;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "ValidateInfo{" +
                    "group='" + group + '\'' +
                    ", dataId='" + dataId + '\'' +
                    ", profile='" + profile + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
