package qunar.tc.qconfig.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LogUtil {
    private static final Logger LOG = LoggerFactory.getLogger("qunar.tc.qconfig");

    public static void log(Action action, QConfigLogLevel logLevel, Object oldValue, Object newValue) {
        switch (logLevel) {
            case off:
                break;
            case low:
                LOG.info("qconfig {} listen, class {}, {} {}, file [{}], group [{}]", action.getType(), action.getClazz().getName(), action.getType(), action.getName(), action.getFile().file, action.getFile().group);
                break;
            case mid:
                LOG.info("qconfig {} listen, class {}, {} {}, value [{}], file [{}], group [{}]", action.getType(), action.getClazz().getName(), action.getType(), action.getName(), newValue, action.getFile().file, action.getFile().group);
                break;
            case high:
                LOG.info("qconfig {} listen, class {}, {} {}, value [{}], old value [{}], file [{}], group [{}]", action.getType(), action.getClazz().getName(), action.getType(), action.getName(), newValue, oldValue, action.getFile().file, action.getFile().group);
                break;
            default:
                throw new IllegalStateException("unknown qconfig log level, [" + logLevel + "]");
        }
    }
}
