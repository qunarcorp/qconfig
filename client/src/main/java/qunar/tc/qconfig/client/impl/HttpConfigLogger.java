package qunar.tc.qconfig.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.Constants;

/**
 * @author zhenyu.nie created on 2014 2014/6/10 11:16
 */
class HttpConfigLogger implements ConfigLogger {

    private static final Logger logger = LoggerFactory.getLogger(HttpConfigLogger.class);

    private final QConfigServerClient client;

    HttpConfigLogger(QConfigServerClient client) {
        this.client = client;
    }

    @Override
    public void log(ConfigLogType type, Meta meta, long version) {
        recordLoading(type, meta, version, Constants.EMPTY);
    }

    @Override
    public void log(ConfigLogType type, Meta meta, long version, String message) {
        recordLoading(type, meta, version, message);
    }

    @Override
    public void log(ConfigLogType type, Meta meta, long version, Throwable ex) {
        recordLoading(type, meta, version, ex.getClass().getName() + ": " + ex.getMessage());
    }

    private void recordLoading(ConfigLogType type, Meta meta, long version, String remarks) {
        try {
            client.recordLoading(type, meta, version, remarks);
        } catch (Throwable e) {
            logger.error("record loading error, type={}, meta={}, version={}, remarks={}", type, meta, version, remarks);
        }
    }
}
