package qunar.tc.qconfig.admin.service.template.exception;

/**
 * @author zhenyu.nie created on 2016/4/4 11:31
 */
public class ValueCheckException extends RuntimeException {
    private static final long serialVersionUID = -5099293794124296965L;

    public ValueCheckException(String message) {
        super(message);
    }
}
