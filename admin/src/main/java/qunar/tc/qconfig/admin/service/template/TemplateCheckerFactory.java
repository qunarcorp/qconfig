package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;

/**
 * @author zhenyu.nie created on 2016/3/29 16:06
 */
public interface TemplateCheckerFactory {

    String type();

    ColumnDetailChecker createDetailChecker(String name);

    ColumnValueChecker createValueChecker(ObjectNode node);
}
