package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.RegularDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.RegularChecker;

/**
 * Created by pingyang.yang on 2019-01-04
 */
@Service
public class RegularCheckerFactory implements TemplateCheckerFactory {
    @Override
    public String type() {
        return "regular";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new RegularDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new RegularChecker(node);
    }
}
