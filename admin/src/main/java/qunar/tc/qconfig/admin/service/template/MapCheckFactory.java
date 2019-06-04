package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.MapDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.MapValueChecker;

@Service
public class MapCheckFactory implements TemplateCheckerFactory {
    @Override
    public String type() {
        return "map";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new MapDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new MapValueChecker(node);
    }
}
