package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;

import java.util.Map;
import java.util.Properties;

class PropertiesProcessor implements Processor {
    private final MapConfig config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    PropertiesProcessor(String appCode, String file, Feature feature,
                        final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = MapConfig.get(appCode, file, feature);
        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                Properties properties = new Properties();
                properties.putAll(conf);
                manager.process(action, properties, logLevel);
            }
        });
    }

    public void process(Object bean) {
        Properties properties = new Properties();
        properties.putAll(config.asMap());
        action.act(bean, properties, logLevel);
    }
}
