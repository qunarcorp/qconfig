package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.JsonDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.JsonChecker;

/**
 * @author lepdou 2017-09-07
 */
@Service
public class JsonCheckerFactory implements TemplateCheckerFactory {
    @Override
    public String type() {
        return "json";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new JsonDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new JsonChecker(node);
    }
}
