package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.BooleanDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.BooleanValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/11 14:18
 */
@Service
public class BooleanCheckFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return "boolean";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new BooleanDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new BooleanValueChecker(node);
    }
}
