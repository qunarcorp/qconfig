package qunar.tc.qconfig.client.spring;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Configuration;

import java.util.ArrayList;
import java.util.List;

class AnnotationListener<T> implements Configuration.ConfigListener<T> {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationListenerManager.class);

    private final List<Configuration.ConfigListener<T>> listeners = new ArrayList<>();

    private T config = null;

    synchronized void add(Configuration.ConfigListener<T> listener) {
        Preconditions.checkNotNull(listener);
        if (config != null) {
            trigger(listener, config);
        }
        listeners.add(listener);
    }

    @Override
    public synchronized void onLoad(T conf) {
        for (Configuration.ConfigListener<T> listener : listeners) {
            trigger(listener, conf);
        }
        config = conf;
    }

    private void trigger(Configuration.ConfigListener<T> listener, T conf) {
        try {
            listener.onLoad(conf);
        } catch (Throwable e) {
            logger.error("配置文件变更, 事件触发异常. data: {}", conf, e);
        }
    }
}