package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.TypedConfig;

class PojoProcessor implements Processor {
    private final TypedConfig<?> config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    PojoProcessor(String appCode, String file, Feature feature, Class<?> clazz,
                  final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = TypedConfig.get(appCode, file, feature, clazz);

        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener() {
            @Override
            public void onLoad(Object value) {
                manager.process(action, value, logLevel);
            }
        });
    }

    public void process(Object bean) {
        action.act(bean, config.current(), logLevel);
    }
}
