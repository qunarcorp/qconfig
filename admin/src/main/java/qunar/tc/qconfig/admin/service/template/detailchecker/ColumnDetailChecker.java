package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zhenyu.nie created on 2016/3/29 16:16
 */
public interface ColumnDetailChecker {

    String name();

    void check(ObjectNode node);

    interface Context {
        boolean isLegalValue(String value);
    }
}
