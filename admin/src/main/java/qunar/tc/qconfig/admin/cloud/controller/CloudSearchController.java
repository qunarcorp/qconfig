package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.PropertiesEntryVo;
import qunar.tc.qconfig.admin.model.PropertiesEntry;
import qunar.tc.qconfig.admin.service.PropertiesEntryService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.ProfileUtil;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author keli.wang
 */
@Controller
public class CloudSearchController extends AbstractControllerHelper {

    @Resource
    private PropertiesEntryService propertiesEntryService;

    @Resource
    private UserContextService userContextService;


    @RequestMapping("/qconfig/file/search")
    @ResponseBody
    public Object search(@RequestParam("key") String key,
                         @RequestParam(value = "group", required = false, defaultValue = "") String group,
                         @RequestParam(value = "env", required = false, defaultValue = "") String env,
                         @RequestParam(value = "buildGroup", required = false, defaultValue = "") String buildGroup,
                         @RequestParam("type") String fileType,
                         @RequestParam(value = "curPage", required = false, defaultValue = "1") int pageNo,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        key = key.trim();
        // 处理env和buildGroup组成的profile
        String profile = env + ":" + buildGroup;
        if (!ProfileUtil.legalProfile(profile)) {
            profile = "";
        }

        if (pageNo < 1) {
            pageNo = 1;
        }

        // 是否是搜索引用文件的请求
        final boolean searchReference = Objects.equal(fileType, "ref");

        final Set<String> validGroups = userContextService.getAccessibleAccountGroups();
        // 拒绝搜索不可访问的应用

        if (CheckUtil.isLegalGroup(group) && !validGroups.contains(group)) {
            return JsonV2.failOf("你没有访问此应用qconfig配置的权限");
        }

        Set<String> searchGroups = validGroups;
        if (validGroups.contains(group)) {
            searchGroups = ImmutableSet.of(group);
        }
        List<PropertiesEntry> entries;
        int totalCount = 0;
        if (searchReference) {
            entries = propertiesEntryService.searchRefEntries(key,
                                                              searchGroups,
                                                              profile);
        } else {
            entries = propertiesEntryService.searchEntries(key,
                                                           searchGroups,
                                                           profile,
                                                           pageNo,
                                                           pageSize);
            totalCount = propertiesEntryService.countEntries(key, searchGroups, profile);
        }

        List<PropertiesEntryVo> entryVos =Lists.newArrayListWithCapacity(entries.size());
        for (PropertiesEntry entry : entries) {
            PropertiesEntryVo entryVo = new PropertiesEntryVo();
            BeanUtils.copyProperties(entry, entryVo);
            String groupName = userContextService.getApplication(entry.getGroupId()).getName();
            entryVo.setGroupName(groupName);
            entryVos.add(entryVo);
        }
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("list", entryVos);
        map.put("totalCount", totalCount);
        return JsonV2.successOf(map);
    }
}
