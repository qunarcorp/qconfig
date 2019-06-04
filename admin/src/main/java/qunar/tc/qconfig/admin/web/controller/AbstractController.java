package qunar.tc.qconfig.admin.web.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.bean.GroupItem;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.util.QConfigAttributes;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 20:39
 */
@Controller
public class AbstractController extends AbstractControllerHelper {

    private static Logger logger = LoggerFactory.getLogger(AbstractController.class);

    @Resource
    private UserContextService userContext;

    @Resource
    private ProfileService profileService;

    @ModelAttribute
    public void loadMenuInfo(Model model) {
        String rtxId = null;
        try {
            model.addAttribute("currentEnv", ServerManager.getInstance().getAppServerConfig().getType());

            QConfigAttributes instance = QConfigAttributesLoader.getInstance();
            model.addAttribute("globalEnv", instance);

            rtxId = userContext.getRtxId();
            if (rtxId == null) {
                model.addAttribute("groupItems", ImmutableList.of());
            } else {
                model.addAttribute("rtxId", rtxId);

                Map<String, GroupItem> map = Maps.newHashMap();
                Set<String> accessibleGroups = userContext.getAccessibleGroups();
                for (String group : accessibleGroups) {
                    map.put(group, userContext.getApplication(group) == null ? new GroupItem(group) : new GroupItem(group, userContext.getApplication(group).getName()));
                }

                List<Map.Entry<String, String>> profileEntries = profileService.find(accessibleGroups);
                for (Map.Entry<String, String> profileEntry : profileEntries) {
                    String group = profileEntry.getKey();
                    String profile = profileEntry.getValue();
                    if (Strings.isNullOrEmpty(profile)
                            || profile.toLowerCase().startsWith("lpt:")) {
                        continue;
                    }
                    GroupItem groupItem = map.get(group);
                    groupItem.add(profile);
                }

                for (GroupItem groupItem : map.values()) {
                    groupItem.sort();
                }

                model.addAttribute("groupItems", Ordering.natural().immutableSortedCopy(map.values()).asList());
            }
        } catch (RuntimeException e) {
            logger.error("load menu error, rtx id=[{}]", rtxId, e);
            throw e;
        }
    }
}
