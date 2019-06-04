package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.IntDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.IntChecker;

/**
 * @author zhenyu.nie created on 2016/3/29 16:07
 */
@Service
public class IntCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return "int";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new IntDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new IntChecker(node);
    }
}
