package qunar.tc.qconfig.admin.exception;

/**
 * @author pingyang.yang
 * @since 2018/9/21
 */
public class CandidateNotExistException extends RuntimeException {
    public CandidateNotExistException() {
        super("Candidate Not exist");
    }

    public CandidateNotExistException(String message) {
        super(message);
    }

    public CandidateNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public CandidateNotExistException(Throwable cause) {
        super("Candidate Not exist", cause);
    }

    public CandidateNotExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
