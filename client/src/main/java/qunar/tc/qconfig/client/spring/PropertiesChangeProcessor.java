package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.PropertiesChange;

import java.util.Collections;
import java.util.Map;

class PropertiesChangeProcessor implements Processor {

    private final MapConfig config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    PropertiesChangeProcessor(String appCode, String file, Feature feature, final Action action,
                              final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = MapConfig.get(appCode, file, feature);
        config.addPropertiesListener(new MapConfig.PropertiesChangeListener() {
            @Override
            public void onChange(PropertiesChange change) {
                manager.process(action, change, logLevel);
            }
        });
    }

    @Override
    public void process(Object bean) {
        Map<String, String> map = config.asMap();
        action.act(bean, PropertiesChange.diff(Collections.EMPTY_MAP, map), logLevel);
    }
}
