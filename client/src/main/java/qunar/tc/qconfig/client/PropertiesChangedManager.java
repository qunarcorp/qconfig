package qunar.tc.qconfig.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zhaohui.yu
 * 11/15/17
 */
class PropertiesChangedManager implements Configuration.ConfigListener<Map<String, String>> {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesChangedManager.class);

    private final CopyOnWriteArrayList<MapConfig.PropertiesChangeListener> propertiesChangeListeners = new CopyOnWriteArrayList<MapConfig.PropertiesChangeListener>();

    private volatile Map<String, String> config = Collections.EMPTY_MAP;

    @Override
    public synchronized void onLoad(Map<String, String> conf) {
        Map<String, String> oldConfig = config;
        config = conf;
        if (propertiesChangeListeners.isEmpty()) return;

        PropertiesChange change = PropertiesChange.diff(oldConfig, config);
        if (!change.hasChange()) return;

        for (MapConfig.PropertiesChangeListener listener : propertiesChangeListeners) {
            triggerPropertiesChange(listener, change);
        }
    }

    private void triggerPropertiesChange(MapConfig.PropertiesChangeListener listener, PropertiesChange change) {
        try {
            listener.onChange(change);
        } catch (Throwable e) {
            logger.error("配置文件变更, 事件触发异常. data: {}", change, e);
        }
    }

    synchronized void addPropertiesListener(MapConfig.PropertiesChangeListener listener) {
        Preconditions.checkNotNull(listener);
        if (!config.equals(Collections.EMPTY_MAP)) {
            PropertiesChange change = new PropertiesChange(config, ImmutableMap.<String, String>of(), ImmutableMap.<String, MapDifference.ValueDifference<String>>of(), ImmutableMap.<String, String>of());
            if (change.hasChange()) {
                triggerPropertiesChange(listener, change);
            }
        }
        propertiesChangeListeners.add(listener);
    }
}
