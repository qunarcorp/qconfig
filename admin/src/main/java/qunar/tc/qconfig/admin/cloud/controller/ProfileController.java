package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import qunar.tc.qconfig.admin.cloud.service.EnvironmentService;
import qunar.tc.qconfig.admin.cloud.vo.ApplicationVo;
import qunar.tc.qconfig.admin.cloud.vo.EnvironmentVo;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaRequest;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.FileDiffInfo;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.admin.support.DiffUtil;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.client.spring.QMapConfig;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.support.Application;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;

import javax.annotation.Resource;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static qunar.tc.qconfig.admin.web.security.RecentlyAccessedFilter.COOKIE_KEY_RECENTLY_ACCESSED_GROUPS;

@Controller
@RequestMapping("/qconfig/profile")
public class ProfileController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final static String RECENTLY_ACCESSED = "recentlyAccessed";

    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    @Resource
    private ProfileService profileService;

    @Resource
    private ConfigService configService;

    @Resource
    private UserContextService userContext;

    @Resource
    private EnvironmentService environmentService;

    @QMapConfig(value = "config.properties", key = "public.group.blacklist")
    private Set<String> ignorePublicApps;

    /**
     * 列出用户下所有的<appcode, <env, group>>
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object listAppProfilesWithOrder(@RequestParam(value = "group", required = false) String groupKey,
                                           @RequestParam(value = "extGroup", required = false) String extGroup,
                                           @RequestParam(required = false, defaultValue = RECENTLY_ACCESSED) String sortBy,
                                           @CookieValue(value = COOKIE_KEY_RECENTLY_ACCESSED_GROUPS, required = false, defaultValue = "")
                                                       String recentlyAccessedGroupsEncoded) {
        Map<String, Map<String, Set<String>>> groupTotalEnvs = userContext.getGroupTotalEnvs();
        // 管理员可以通过extGroup参数查看其他group
        if (CheckUtil.isLegalGroup(extGroup) && !groupTotalEnvs.containsKey(extGroup) && userContext.isAdmin()) {
            Map<String, Set<String>> extGroupEnvs = userContext.getGroupEnvs(extGroup);
            if (!CollectionUtils.isEmpty(extGroupEnvs)) {
                groupTotalEnvs.put(extGroup, extGroupEnvs);
            }
        }
        Map<String, Map<String, Set<String>>> filteredGroupEnvs = filterGroups(groupTotalEnvs, groupKey);
        String recentlyAccessedGroupsStr = new String(BaseEncoding.base64Url().omitPadding().decode(recentlyAccessedGroupsEncoded), Charsets.UTF_8);
        List<String> recentlyAccessedGroups = SPLITTER.splitToList(recentlyAccessedGroupsStr);
        final Map<String, Integer> recentlyAccessedGroupsPriority = Maps.newHashMapWithExpectedSize(recentlyAccessedGroups.size());
        for (int i = 0; i < recentlyAccessedGroups.size(); ++i) {
            recentlyAccessedGroupsPriority.put(recentlyAccessedGroups.get(i), i);
        }
        boolean sortByRecentlyAccessed = RECENTLY_ACCESSED.equals(sortBy);
        List<ApplicationVo> recentApplicationVos = Lists.newArrayListWithCapacity(filteredGroupEnvs.size());
        List<ApplicationVo> applicationVos = Lists.newArrayListWithCapacity(filteredGroupEnvs.size());
        for (Map.Entry<String, Map<String, Set<String>>> groupEnvsEntry : filteredGroupEnvs.entrySet()) {
            String group = groupEnvsEntry.getKey();
            Application app = userContext.getApplication(group);
            Map<String, Set<String>> groupEnvs = groupEnvsEntry.getValue();
            ApplicationVo applicationVo = trans(app, groupEnvs);
            if (sortByRecentlyAccessed && recentlyAccessedGroupsPriority.containsKey(applicationVo.getAppcode())) {
                recentApplicationVos.add(applicationVo);
            } else {
                applicationVos.add(applicationVo);
            }
        }
        recentApplicationVos.sort(new Comparator<ApplicationVo>() {
            @Override
            public int compare(ApplicationVo o1, ApplicationVo o2) {
                return recentlyAccessedGroupsPriority.get(o1.getAppcode()).compareTo(recentlyAccessedGroupsPriority.get(o2.getAppcode()));
            }
        });
        applicationVos.sort(new Comparator<ApplicationVo>() {
            @Override
            public int compare(ApplicationVo o1, ApplicationVo o2) {
                return o1.getAppcode().toLowerCase().compareTo(o2.getAppcode().toLowerCase());
            }
        });
        recentApplicationVos.addAll(applicationVos);
        return JsonV2.successOf(recentApplicationVos);
    }
    /**
     * 列出用户指定的group的环境信息
     */
    @RequestMapping("/listAppProfile")
    @ResponseBody
    public Object listAppProfile(@RequestParam(value = "group") String groupKey){
        if(ignorePublicApps.contains(groupKey)){
            return new JsonV2<>(-1, "app not exist", null);
        }
        Application app;
        try {
            app = userContext.getApplication(groupKey);
        }catch (Exception e){
            return new JsonV2<>(-1, "app not exist", null);
        }
        Map<String, Set<String>> groupEnvs = userContext.getGroupEnvs(groupKey);
        ApplicationVo applicationVo = trans(app, groupEnvs);
        List<ApplicationVo> applicationVos = Lists.newArrayListWithCapacity(1);
        applicationVos.add(applicationVo);
        return JsonV2.successOf(applicationVos);
    }

    // TODO: 2018/10/17
    @RequestMapping("hasGreyRelease")
    @ResponseBody
    public Object hasGreyRelease(String group, String profile) {
        return JsonV2.success();
    }

    private Map<String, Map<String, Set<String>>> filterGroups(Map<String, Map<String, Set<String>>> allGroupEnvs, String groupKey) {
        if (StringUtils.isEmpty(groupKey)) {
            return allGroupEnvs;
        }
        Map<String, Map<String, Set<String>>> filteredGroupEnvs = Maps.newHashMap();
        for (Map.Entry<String, Map<String, Set<String>>> groupEnvsEntry : allGroupEnvs.entrySet()) {
            // 搜索过滤group, group或name中包含搜索关键字均可
            String group = groupEnvsEntry.getKey();
            String groupName = Strings.nullToEmpty(userContext.getApplication(group).getName());
            if (StringUtils.isNotEmpty(group) && (group.contains(groupKey) || groupName.contains(groupKey))) {
                filteredGroupEnvs.put(groupEnvsEntry.getKey(), groupEnvsEntry.getValue());
            }
        }
        return filteredGroupEnvs;
    }

    private ApplicationVo trans(Application app, Map<String, Set<String>> envMap) {
        String appcode = app.getCode();
        String name = app.getName();
        if (envMap == null) {
            return new ApplicationVo(appcode, name, ImmutableList.<EnvironmentVo>of());
        }
        List<EnvironmentVo> envs = Lists.newArrayListWithCapacity(envMap.size());
        for (Map.Entry<String, Set<String>> envEntry : envMap.entrySet()) {
            EnvironmentVo environmentVo = trans(envEntry.getKey(), envEntry.getValue());
            envs.add(environmentVo);
        }
        // sort special envs ("resources, prod, beta, dev")
        Collections.sort(envs, new Comparator<EnvironmentVo>() {

            @Override
            public int compare(EnvironmentVo o1, EnvironmentVo o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });

        return new ApplicationVo(appcode, name, envs);
    }

    private EnvironmentVo trans(String env, Set<String> buildGroupSet) {
        List<String> sortedBuildGroupList = Ordering.natural().sortedCopy(buildGroupSet);
        EnvironmentVo envVo = new EnvironmentVo(env, sortedBuildGroupList);
        Map<String, Integer> envOrderMap = environmentService.getEnvDisplayOrders();
        if (envOrderMap.containsKey(env)) {
            envVo.setOrder(envOrderMap.get(env));
        }
        return envVo;
    }

    @RequestMapping(value = "/envTypes", method = RequestMethod.GET)
    @ResponseBody
    public Object getEnvType() {
        Map<String, String> envTypeMap = Maps.newHashMap();
        List<String> envDefaults = environmentService.getSystemDefaultEnvs();
        for (String env : envDefaults) {
            String envType = Environment.fromEnvName(env).getEnvType().getName();
            envTypeMap.put(env, envType);
        }

        return JsonV2.successOf(envTypeMap);
    }

    @RequestMapping(value = "/createBuildGroup", method = RequestMethod.POST)
    @ResponseBody
    public Object createBuildGroup(@RequestBody FileMetaRequest profileMeta) {
        String group = profileMeta.getGroup();
        String profile = profileMeta.getProfile();
        checkLegalGroup(group);
        checkLegalProfile(profile);

        logger.info("create profile, group=[{}], profile=[{}]", group, profile);
        String buildGroup = ProfileUtil.getBuildGroup(profile);
        checkArgument(buildGroup.length() > 0 && buildGroup.length() <= Environment.BUILD_GROUP_MAX_LENGTH,
                QConfigAttributesLoader.getInstance().getBuildGroup() + "长度必须在1到" + Environment.BUILD_GROUP_MAX_LENGTH + "之间");
        checkArgument(ProfileUtil.BUILD_GROUP_LETTER_DIGIT_PATTERN.matcher(buildGroup).find(),
                QConfigAttributesLoader.getInstance().getBuildGroup() + "不能包含[小写字符，数字，'_'，'-']以外的其它内容");

        try {
            profileService.create(group, profile);
            return JsonV2.successOf(true);
        } catch (ModifiedException e) {
            logger.info("{} has been created before, group=[{}], profile=[{}]", QConfigAttributesLoader.getInstance().getBuildGroup(), group, profile);
            throw new RuntimeException(QConfigAttributesLoader.getInstance().getBuildGroup() + "已经存在");
        } catch (Exception e) {
            logger.error("create {} error, group=[{}], profile=[{}]", QConfigAttributesLoader.getInstance().getBuildGroup(), group, profile, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    @ResponseBody
    public Object compare(@RequestParam("group") String group,
                          @RequestParam("lProfile") String lProfile,
                          @RequestParam("rProfile") String rProfile) {
        checkLegalGroup(group);
        checkLegalProfile(lProfile);
        checkLegalProfile(rProfile);

        boolean isLDefaultProfile = defaultProfile(lProfile);
        boolean isRDefaultProfile = defaultProfile(rProfile);
        if (!isLDefaultProfile || !isRDefaultProfile) {
            List<String> profiles;
            try {
                profiles = profileService.find(group);
            } catch (RuntimeException e) {
                logger.error("compare profiles error, group=[{}], lProfile=[{}], rProfile=[{}]", group, lProfile, rProfile, e);
                throw e;
            }
            Preconditions.checkArgument(isLDefaultProfile || profiles.contains(lProfile), "不存在的profile: [%s]", lProfile);
            Preconditions.checkArgument(isRDefaultProfile || profiles.contains(rProfile), "不存在的profile: [%s]", rProfile);
        }

        Monitor.PROD_BETA_COMPARE_STATICS.inc();
        try {
            List<FileDiffInfo> diffInfos = configService.diffProfile(group, lProfile, rProfile, DiffUtil.DiffType.HTML);
            List<FileDiffInfo> result = Lists.newArrayListWithCapacity(diffInfos.size());
            result.addAll(diffInfos);
            return JsonV2.successOf(Ordering.from(DIFF_INFO_COMPARATOR).reverse().immutableSortedCopy(result));
        } catch (RuntimeException e) {
            logger.error("compare profiles error, group=[{}], lProfile=[{}], rProfile=[{}]", group, lProfile, rProfile, e);
            return JsonV2.failOf("profile对比异常");
        }
    }

    private boolean defaultProfile(String profile) {
        return Environment.fromProfile(profile).defaultProfile().equals(profile);
    }

    public static final Comparator<FileDiffInfo> DIFF_INFO_COMPARATOR = new Comparator<FileDiffInfo>() {
        @Override
        public int compare(FileDiffInfo lhs, FileDiffInfo rhs) {
            if (lhs.getDiff() == null && rhs.getDiff() != null) {
                return -1;
            } else if (lhs.getDiff() != null && rhs.getDiff() == null) {
                return 1;
            } else if (lhs.getDiff() == null && rhs.getDiff() == null) {
                if (!Strings.isNullOrEmpty(lhs.getError()) && Strings.isNullOrEmpty(rhs.getError())) {
                    return 1;
                } else if (Strings.isNullOrEmpty(lhs.getError()) && !Strings.isNullOrEmpty(rhs.getError())) {
                    return -1;
                }
            }

            if (!Strings.isNullOrEmpty(lhs.getError()) && Strings.isNullOrEmpty(rhs.getError())) {
                return -1;
            } else if (Strings.isNullOrEmpty(lhs.getError()) && !Strings.isNullOrEmpty(rhs.getError())) {
                return 1;
            }

            if (!allExist(lhs) && allExist(rhs)) {
                return -1;
            } else if (allExist(lhs) && !allExist(rhs)) {
                return 1;
            }

            return lhs.getName().compareTo(rhs.getName());
        }

        private boolean allExist(FileDiffInfo diffInfo) {
            return diffInfo.isLExist() && diffInfo.isRExist();
        }
    };

}
