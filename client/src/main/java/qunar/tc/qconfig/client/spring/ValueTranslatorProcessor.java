package qunar.tc.qconfig.client.spring;

import com.google.common.base.Strings;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.impl.QConfigTranslator;
import qunar.tc.qconfig.client.impl.Translators;

import java.lang.reflect.Type;
import java.util.Map;

class ValueTranslatorProcessor implements Processor {
    private final MapConfig config;
    private final String key;
    private final String defaultValue;
    private final QConfigTranslator translator;
    private final Action action;
    private final QConfigLogLevel logLevel;

    ValueTranslatorProcessor(Type genericType, final String key, final String defaultValue,
                             String appCode, String file, Feature feature,
                             final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.translator = Translators.getInternalTranslator(genericType);
        this.action = action;
        this.logLevel = logLevel;
        this.config = MapConfig.get(appCode, file, feature);

        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                manager.process(action, getValue(conf), logLevel);
            }
        });
    }

    public void process(Object bean) {
        action.act(bean, getValue(config.asMap()), logLevel);
    }

    private Object getValue(Map<String, String> conf) {
        String value = conf.get(key);
        if (Strings.isNullOrEmpty(value)) {
            value = defaultValue;
        }
        final String inputValue = value;
        return translator.translate(inputValue);
    }
}