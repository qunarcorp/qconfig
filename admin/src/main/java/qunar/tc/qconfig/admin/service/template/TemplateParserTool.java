package qunar.tc.qconfig.admin.service.template;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.Strings;
import qunar.tc.qconfig.common.util.TemplateMergeParser;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class TemplateParserTool {

    private final Logger logger = LoggerFactory.getLogger(TemplateParserTool.class);

    private final static String PLACEHOLDER_GROUP = "_appcode";
    private final static String PLACEHOLDER_DATA_ID = "_filename";
    private final static String PLACEHOLDER_PROFILE = "_profile";
    private final static String PLACEHOLDER_ENV = "_env";
    private final static String PLACEHOLDER_BUILD_GROUP = "_buildGroup";

    private volatile boolean parserSwitch;

    @PostConstruct
    private void init() {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                parserSwitch = Strings.getBoolean(conf.get("template.parseEnvironmentVariables.switch"), false);
            }
        });
    }

    public String parse(ConfigMeta meta, String content) {
        if (!parserSwitch) {
            return content;
        }
        TemplateMergeParser parser = new TemplateMergeParser(genParameters(meta));
        TemplateMergeParser.Result result = parser.mergeParse(content);
        logger.info("替换模板配置:[{}], 替换参数[{}]", meta, result.parameters);
        return result.content;
    }

    private Map<String, String> genParameters(ConfigMeta meta) {
        return ImmutableMap.<String, String>builder()
                .put(PLACEHOLDER_GROUP, meta.getGroup())
                .put(PLACEHOLDER_DATA_ID, meta.getDataId())
                .put(PLACEHOLDER_PROFILE, meta.getProfile())
                .put(PLACEHOLDER_ENV, ProfileUtil.getEnvironment(meta.getProfile()))
                .put(PLACEHOLDER_BUILD_GROUP, ProfileUtil.getBuildGroup(meta.getProfile()))
                .build();
    }
}
