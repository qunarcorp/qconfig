package qunar.tc.qconfig.admin.service.template.valuechecker;

/**
 * @author zhenyu.nie created on 2016/3/29 14:52
 */
public interface ColumnValueChecker {

    String name();

    void check(String value);

    void checkWithoutNullable(String value);
}
