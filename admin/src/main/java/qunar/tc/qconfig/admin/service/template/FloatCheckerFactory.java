package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.FloatDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.FloatChecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 18:06
 */
@Service
public class FloatCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return "float";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new FloatDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new FloatChecker(node);
    }
}
