package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.TypedConfig;

class StringProcessor implements Processor {
    private final TypedConfig<String> config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    StringProcessor(String appCode, String file, Feature feature,
                    final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = TypedConfig.get(appCode, file, feature, TypedConfig.STRING_PARSER);

        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener<String>() {
            @Override
            public void onLoad(String conf) {
                manager.process(action, conf, logLevel);
            }
        });
    }

    public void process(Object bean) {
        action.act(bean, config.current(), logLevel);
    }
}
