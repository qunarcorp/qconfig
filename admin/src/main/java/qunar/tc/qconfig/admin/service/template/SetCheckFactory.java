package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.SetDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.SetValueChecker;

@Service
public class SetCheckFactory implements TemplateCheckerFactory {
    @Override
    public String type() {
        return "set";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new SetDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new SetValueChecker(node);
    }
}
