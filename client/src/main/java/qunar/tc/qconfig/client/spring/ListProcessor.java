package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.TypedConfig;

class ListProcessor implements Processor {

    private final TypedConfig<?> config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    ListProcessor(String appCode, String file, Feature feature, Class<?> listBeanClass,
                  final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = TypedConfig.getList(appCode, file, feature, listBeanClass);
        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener() {
            @Override
            public void onLoad(Object conf) {
                manager.process(action, conf, logLevel);

            }
        });
    }

    @Override
    public void process(Object bean) {
        action.act(bean, config.current(), logLevel);
    }
}
