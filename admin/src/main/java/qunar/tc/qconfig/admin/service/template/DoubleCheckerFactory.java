package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.DoubleDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.DoubleChecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 18:06
 */
@Service
public class DoubleCheckerFactory implements TemplateCheckerFactory {
    @Override
    public String type() {
        return "double";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new DoubleDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new DoubleChecker(node);
    }
}
