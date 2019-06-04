package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.format.DateTimeFormatter;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

/**
 * @author zhenyu.nie created on 2016 2016/10/12 12:04
 */
public class DateValueChecker extends AbstractTimeRelativeValueChecker {

    public DateValueChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected DateTimeFormatter getFormatter() {
        return TemplateContants.DATE_FORMATTER;
    }
}
