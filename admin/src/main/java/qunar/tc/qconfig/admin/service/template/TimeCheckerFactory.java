package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.TimeDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.TimeValueChecker;

/**
 * @author zhenyu.nie created on 2016 2016/10/11 12:31
 */
@Service
public class TimeCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return TemplateContants.TIME_TYPE;
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new TimeDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new TimeValueChecker(node);
    }
}
