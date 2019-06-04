package qunar.tc.qconfig.admin.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.Resource;

@Controller
@RequestMapping("/qconfig/editorSettings")
public class FileEditorController {

    @Resource
    private ConfigEditorSettingsService configEditorSettingsService;

    @RequestMapping("/switchAdvancedEditor")
    @ResponseBody
    public Object switchAdvancedEditor(@RequestParam("group") String groupId,
                                        @RequestParam("dataId") final String dataId,
                                        @RequestParam("useAdvancedEditor") final boolean useAdvancedEditor) {
        final int affectedRows = configEditorSettingsService.updateUseAdvancedEditor(groupId, dataId, useAdvancedEditor);
        return JsonV2.successOf(affectedRows != 0);
    }

}
