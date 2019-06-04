package qunar.tc.qconfig.client.exception;

/**
 * Created by dongcao on 2018/6/4.
 */
public class ResultUnexpectedException extends RuntimeException {

    public static int NOT_SET = -1;

    /**
     * http状态码
     */
    private int status;

    /**
     * 子状态码，用于展现服务端更多状态
     */
    private int subStatus;

    public ResultUnexpectedException(String message) {
        this(NOT_SET, NOT_SET, message);
    }

    public ResultUnexpectedException(int status, String message) {
        this(status, NOT_SET, message);
    }

    public ResultUnexpectedException(int status, int subStatus, String message) {
        super(message);
        this.status = status;
        this.subStatus = subStatus;
    }

    public int getStatus() {
        return status;
    }

    public int getSubStatus() {
        return subStatus;
    }

}
