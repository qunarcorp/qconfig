package qunar.tc.qconfig.admin.exception;

/**
 * Created by @author zhuYongMing on 2018/12/26.
 */
public class HighTrafficException extends RuntimeException {

    public HighTrafficException() {
        super("high traffic");
    }

    public HighTrafficException(String message) {
        super(message);
    }

    public HighTrafficException(String message, Throwable cause) {
        super(message, cause);
    }

    public HighTrafficException(Throwable cause) {
        super("high traffic", cause);
    }

    public HighTrafficException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
