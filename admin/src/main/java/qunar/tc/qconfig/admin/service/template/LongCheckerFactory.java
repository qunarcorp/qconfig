package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.LongDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.LongChecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 18:05
 */
@Service
public class LongCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return "long";
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new LongDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new LongChecker(node);
    }
}
