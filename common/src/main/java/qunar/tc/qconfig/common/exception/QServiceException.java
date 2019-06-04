package qunar.tc.qconfig.common.exception;

/**
 * qconfig Service Exception
 *
 * Created by chenjk on 2017/11/6.
 */
public class QServiceException extends Exception {

    public QServiceException() {
        super();
    }

    public QServiceException(String message) {
        super(message);
    }

    public QServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public QServiceException(Throwable cause) {
        super(cause);
    }
}
