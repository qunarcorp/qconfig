package qunar.tc.qconfig.admin.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import qunar.tc.qconfig.client.MapConfig;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.common.bean.JsonV2;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("/qconfig/help")
public class HelpController {

    private String wikiUrl;

    @PostConstruct
    private void init() {
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(conf -> wikiUrl = conf.get("wiki.url"));
    }

    @RequestMapping("/wiki")
    @ResponseBody
    public Object getWikiUrl() {
        return JsonV2.successOf(wikiUrl);
    }
}
