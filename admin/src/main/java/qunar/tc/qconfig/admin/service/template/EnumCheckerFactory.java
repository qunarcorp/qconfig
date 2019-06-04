package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.EnumDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.EnumChecker;

/**
 * @author zhenyu.nie created on 2016/3/29 16:08
 */
@Service
public class EnumCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return "enum";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new EnumDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new EnumChecker(node);
    }
}
