package qunar.tc.qconfig.admin.exception;

/**
 * @author zhenyu.nie created on 2017 2017/2/21 12:50
 */
public class ValidateErrorException extends RuntimeException {

    private Object error;

    public ValidateErrorException(Object error) {
        this.error = error;
    }

    public ValidateErrorException(String message, Object error) {
        super(message);
        this.error = error;
    }

    public ValidateErrorException(String message, Throwable cause, Object error) {
        super(message, cause);
        this.error = error;
    }

    public ValidateErrorException(Throwable cause, Object error) {
        super(cause);
        this.error = error;
    }

    public Object getError() {
        return error;
    }
}
