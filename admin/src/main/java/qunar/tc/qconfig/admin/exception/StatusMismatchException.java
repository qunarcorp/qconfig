package qunar.tc.qconfig.admin.exception;

/**
 * User: zhaohuiyu
 * Date: 5/23/14
 * Time: 5:05 PM
 */
public class StatusMismatchException extends RuntimeException {
    private static final long serialVersionUID = 3303191574555943554L;

    public StatusMismatchException() {
    }

    public StatusMismatchException(String message) {
        super(message);
    }
}
