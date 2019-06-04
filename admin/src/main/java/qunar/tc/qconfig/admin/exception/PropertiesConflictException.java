package qunar.tc.qconfig.admin.exception;

/**
 * @author zhenyu.nie created on 2016 2016/12/13 17:02
 */
public class PropertiesConflictException extends RuntimeException {

    private String key;

    public PropertiesConflictException(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
