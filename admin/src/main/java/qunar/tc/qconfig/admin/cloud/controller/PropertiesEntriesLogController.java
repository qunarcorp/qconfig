package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.service.PropertiesEntryLogService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.Set;

@Controller
@RequestMapping("/qconfig/propertiesLog")
public class PropertiesEntriesLogController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(PropertiesEntriesLogController.class);

    @Resource
    private UserContextService userContext;

    @Resource
    private PropertiesEntryLogService propertiesEntryLogService;

    @RequestMapping(value = "/listEntries", method = RequestMethod.GET)
    @ResponseBody
    public Object listPropertiesEntries(@RequestParam(required = false, defaultValue = "") final String group,
                                        @RequestParam(required = false, defaultValue = "") String profile,
                                        @RequestParam(required = false, defaultValue = "") String dataId,
                                        @RequestParam(required = false, defaultValue = "") String key,
                                        @RequestParam(required = false, defaultValue = "") final String groupLike,
                                        @RequestParam(required = false, defaultValue = "") String profileLike,
                                        @RequestParam(required = false, defaultValue = "") String dataIdLike,
                                        @RequestParam(required = false, defaultValue = "") String keyLike,
                                        @RequestParam(required = false, defaultValue = "1") int page,
                                        @RequestParam(required = false, defaultValue = "15") int pageSize) {
        // 参数group/profile/dataId/key非空时，为精确搜索条件, 若为空则用*Like参数做模糊搜索
        Set<String> accessibleGroups = userContext.getAccessibleGroups();
        Set<String> validGroups = Sets.filter(accessibleGroups, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return Strings.isNullOrEmpty(group) ? input.contains(groupLike) : input.equals(group);
            }
        });
        try {
            return JsonV2.successOf(
                    propertiesEntryLogService.listEntries(
                            validGroups, profile, dataId, key,profileLike, dataIdLike, keyLike, page, pageSize));
        } catch (Exception e) {
            logger.error("list properties entries error, group:{}, profile:{}, dataId:{}, key:{}, " +
                    "groupLike:{}, profileLike:{}, dataIdLike{}, keyLike:{}, page:{}, pageSize:{}",
                    group, profile, dataId, key, groupLike, profileLike, dataIdLike, keyLike, page, pageSize, e);
            return JsonV2.failOf("内部错误,请联系TCDEV");
        }
    }

    @RequestMapping(value = "/listEntryLogs", method = RequestMethod.GET)
    @ResponseBody
    public Object listEntryLogs(@RequestParam String group,
                                @RequestParam String profile,
                                @RequestParam String dataId,
                                @RequestParam String key,
                                @RequestParam(required = false, defaultValue = "1") int page,
                                @RequestParam(required = false, defaultValue = "10") int pageSize) {

        try {
            return JsonV2.successOf(propertiesEntryLogService.listEntryLogs(new ConfigMeta(group, dataId, profile), key, page, pageSize));
        } catch (Exception e) {
            logger.error("list properties entries error, group:{}, profile:{}, dataId:{}, key:{}, page:{}, pageSize:{}",
                    group, profile, dataId, key, page, pageSize, e);
            return JsonV2.failOf("内部错误,请联系TCDEV");
        }
    }
}
