package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.DateDetailChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.DateValueChecker;

/**
 * @author zhenyu.nie created on 2016 2016/10/12 11:53
 */
@Service
public class DateCheckerFactory implements TemplateCheckerFactory {

    @Override
    public String type() {
        return TemplateContants.DATE_TYPE;
    }

    @Override
    public ColumnDetailChecker createDetailChecker(String name) {
        return new DateDetailChecker(name);
    }

    @Override
    public ColumnValueChecker createValueChecker(ObjectNode node) {
        return new DateValueChecker(node);
    }
}
