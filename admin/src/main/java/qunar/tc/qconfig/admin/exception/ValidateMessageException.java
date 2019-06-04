package qunar.tc.qconfig.admin.exception;

/**
 * @author zhenyu.nie created on 2017 2017/2/21 12:40
 */
public class ValidateMessageException extends RuntimeException {
    public ValidateMessageException() {
    }

    public ValidateMessageException(String message) {
        super(message);
    }

    public ValidateMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidateMessageException(Throwable cause) {
        super(cause);
    }
}
