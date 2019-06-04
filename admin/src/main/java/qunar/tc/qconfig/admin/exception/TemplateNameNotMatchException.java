package qunar.tc.qconfig.admin.exception;

/**
 * Created by pingyang.yang on 2018/11/14
 */
public class TemplateNameNotMatchException extends RuntimeException {

    public TemplateNameNotMatchException() {
        super("file limit name not match template limit");
    }
}
