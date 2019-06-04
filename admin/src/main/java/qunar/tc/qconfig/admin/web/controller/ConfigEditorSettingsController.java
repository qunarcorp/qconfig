package qunar.tc.qconfig.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.Resource;

/**
 * @author keli.wang
 * @since 2017/5/15
 */
@Controller
@RequestMapping("/view/configEditorSettings")
public class ConfigEditorSettingsController {
    @Resource
    private ConfigEditorSettingsService configEditorSettingsService;

    @RequestMapping("/switchAdvancedEditor")
    @ResponseBody
    public JsonV2 switchAdvancedEditor(@RequestParam("group") final String groupId,
                                     @RequestParam("dataId") final String dataId,
                                     @RequestParam("useAdvancedEditor") final boolean useAdvancedEditor) {
        final int affectedRows = configEditorSettingsService.updateUseAdvancedEditor(groupId, dataId, useAdvancedEditor);
        return JsonV2.successOf(affectedRows != 0);
    }
}
