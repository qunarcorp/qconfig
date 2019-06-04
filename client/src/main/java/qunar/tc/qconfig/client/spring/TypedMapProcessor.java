package qunar.tc.qconfig.client.spring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.impl.QConfigMapTranslator;
import qunar.tc.qconfig.client.impl.QConfigTranslator;
import qunar.tc.qconfig.client.impl.Translators;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

class TypedMapProcessor implements Processor {
    private final TranslatorProcessor processor;

    TypedMapProcessor(Type genericType, final String appCode, String file, Feature feature,
                      final Action action, final QConfigLogLevel logLevel, AnnotationManager manager) {

        Type keyType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        Type valueType = ((ParameterizedType) genericType).getActualTypeArguments()[1];
        final QConfigTranslator keyTranslator = Translators.getInternalTranslator(keyType);
        final QConfigTranslator valueTranslator = Translators.getInternalTranslator(valueType);
        QConfigMapTranslator translator = new QConfigMapTranslator<Object>() {
            @Override
            public Object translate(Map<String, String> map) {
                Map<Object, Object> tempMap = Maps.newHashMap();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    tempMap.put(keyTranslator.translate(entry.getKey()), valueTranslator.translate(entry.getValue()));
                }
                return ImmutableMap.copyOf(tempMap);
            }
        };
        this.processor = new TranslatorProcessor(translator, appCode, file, feature, action, logLevel, manager);
    }

    public void process(Object bean) {
        processor.process(bean);
    }
}
