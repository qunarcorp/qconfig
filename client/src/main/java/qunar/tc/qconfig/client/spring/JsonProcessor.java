package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.JsonConfig;

import java.lang.reflect.Type;

class JsonProcessor implements Processor {
    private final JsonConfig config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    JsonProcessor(Type genericType, String appCode, String file, Feature feature, final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = JsonConfig.get(appCode, file, feature, JsonConfig.ParameterizedClass.of(genericType));
        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener() {
            @Override
            public void onLoad(Object conf) {
                manager.process(action, conf, logLevel);
            }
        });
    }

    public void process(Object bean) {
        action.act(bean, config.current(), logLevel);
    }
}
