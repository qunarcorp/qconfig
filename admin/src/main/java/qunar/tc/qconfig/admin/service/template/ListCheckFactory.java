package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.ListDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ListValueChecker;

@Service
public class ListCheckFactory implements TemplateCheckerFactory {
    @Override
    public String type() {
        return "list";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new ListDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new ListValueChecker(node);
    }
}
