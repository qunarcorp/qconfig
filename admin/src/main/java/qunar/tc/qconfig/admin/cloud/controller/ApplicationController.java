package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.service.ApplicationInfoService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.support.Application;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;

@Controller
@RequestMapping(value = "/qconfig/app")
public class ApplicationController {

    @Resource
    private UserContextService userContext;

    @Resource
    private ApplicationInfoService applicationInfoService;

    @RequestMapping(value = "/appCode/envGroups", method = RequestMethod.GET)
    @ResponseBody
    public Object listApplicationEnvAndGroups(@RequestParam("group") String appCode) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appCode), "appCode不能为空");
        Map<String, Map<String, Set<String>>> groupTotalEnvs = userContext.getGroupTotalEnvs();
        Map<String, Set<String>> appEnvGroups = groupTotalEnvs.get(appCode);
        Set<String> result = Sets.newLinkedHashSet();
        for (String env : appEnvGroups.keySet()) {
            result.add(env + ":");
            for (String group : appEnvGroups.get(env)) {
                result.add(env + ":" + nullToEmpty(group).trim());
            }
        }
        return JsonV2.successOf(result);
    }

    @RequestMapping(value = "/appCode/getToken", method = RequestMethod.GET)
    @ResponseBody
    public Object getToken(String appCode) {
        return JsonV2.successOf(0, "success", applicationInfoService.getToken(appCode));
    }

    @RequestMapping(value = "/appCode/create", method = RequestMethod.POST)
    @ResponseBody
    public Object createApplication (@RequestBody MockApplication application) {
        Preconditions.checkNotNull(application, "application 不能为空");
//        Preconditions.checkNotNull(application.getCode(), "appCode不能为空");
        Preconditions.checkArgument(!applicationInfoService.checkExist(application.code), "appCode已存在");
        List<String> owner = Lists.newArrayList(userContext.getRtxId());
        applicationInfoService.createApplication(new Application(application.code, application.name, "", null, owner, null, Application.Status.pass, "", new Date()));

        return JsonV2.success();
    }

    @RequestMapping(value = "updateApplication", method = RequestMethod.POST)
    @ResponseBody
    public Object updateApplication(@RequestBody Application application) {
        Preconditions.checkNotNull(application, "application 不能为空");
        Preconditions.checkNotNull(application.getCode(), "appCode不能为空");

        applicationInfoService.updateApplication(application);
        return JsonV2.success();
    }

    public static class MockApplication {
        private String code;

        private String name;

        public MockApplication() {
        }

        public MockApplication(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
