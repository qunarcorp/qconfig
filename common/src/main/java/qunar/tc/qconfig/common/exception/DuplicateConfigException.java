package qunar.tc.qconfig.common.exception;

public class DuplicateConfigException extends RuntimeException {
    private static final long serialVersionUID = -2971027482886115437L;

    public DuplicateConfigException(final String message) {
        super(message);
    }
}
