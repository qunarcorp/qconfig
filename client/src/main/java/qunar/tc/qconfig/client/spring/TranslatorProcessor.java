package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.impl.QConfigMapTranslator;

import java.util.Map;

class TranslatorProcessor implements Processor {
    private final MapConfig config;
    private final QConfigMapTranslator translator;
    private final Action action;
    private final QConfigLogLevel logLevel;

    TranslatorProcessor(final QConfigMapTranslator translator, final String appCode, String file, Feature feature,
                        final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.translator = translator;
        this.action = action;
        this.logLevel = logLevel;
        config = MapConfig.get(appCode, file, feature);
        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(final Map<String, String> conf) {
                manager.process(action, translator.translate(conf), logLevel);
            }
        });

    }

    public void process(Object bean) {
        action.act(bean, translator.translate(config.asMap()), logLevel);
    }
}
