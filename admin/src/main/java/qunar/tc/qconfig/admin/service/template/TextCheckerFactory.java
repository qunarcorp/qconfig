package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.TextDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.TextChecker;

/**
 * @author zhenyu.nie created on 2016/3/29 16:10
 */
@Service
public class TextCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return "text";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new TextDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new TextChecker(node);
    }
}
