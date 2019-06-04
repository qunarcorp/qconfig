package qunar.tc.qconfig.client.spring;

import org.springframework.core.annotation.AnnotationUtils;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.TypedConfig;

import java.util.Map;

class BeanProcessor {
    private volatile Action action;

    private QConfigLogLevel logLevel;

    private TypedConfig<?> typedConfig;

    BeanProcessor(final Class<?> clazz, final AnnotationManager manager) {
        QMapConfig annotation = AnnotationUtils.getAnnotation(clazz, QMapConfig.class);
        if (annotation == null) return;

        Map.Entry<Util.File, Feature> fileInfo = manager.getFileInfo(annotation.value());
        Util.File file = fileInfo.getKey();
        Feature feature = fileInfo.getValue();
        logLevel = annotation.logLevel();
        typedConfig = TypedConfig.get(file.group, file.file, feature, clazz);
        action = new BeanInject(file, clazz, manager);

        AnnotationListenerManager.getInstance().addAnnotationListener(typedConfig, new Configuration.ConfigListener() {
            @Override
            public void onLoad(Object newValue) {
                manager.process(action, newValue, logLevel);
            }
        });
    }

    public boolean process(Object bean) {
        if (action == null) return false;
        action.act(bean, typedConfig.current(), logLevel);
        return true;
    }
}
