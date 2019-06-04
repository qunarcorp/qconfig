package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author zhenyu.nie created on 2018 2018/9/19 16:13
 */
class AnnotationListenerManager {

    private static final AnnotationListenerManager INSTANCE = new AnnotationListenerManager();

    static AnnotationListenerManager getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<Configuration, AnnotationListener> listeners = new ConcurrentHashMap<>();

    private AnnotationListenerManager() {
    }

    @SuppressWarnings("all")
    void addAnnotationListener(Configuration config, Configuration.ConfigListener listener) {
        AnnotationListener annotationListener = listeners.get(config);
        if (annotationListener != null) {
            annotationListener.add(listener);
            return;
        }

        annotationListener = new AnnotationListener();
        AnnotationListener old = listeners.putIfAbsent(config, annotationListener);
        if (old != null) {
            annotationListener = old;
        }
        annotationListener.add(listener);
        config.addListener(annotationListener);
    }
}
