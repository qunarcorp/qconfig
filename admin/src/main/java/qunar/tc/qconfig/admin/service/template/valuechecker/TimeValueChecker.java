package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.format.DateTimeFormatter;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

/**
 * @author zhenyu.nie created on 2016 2016/10/11 12:42
 */
public class TimeValueChecker extends AbstractTimeRelativeValueChecker {

    public TimeValueChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected DateTimeFormatter getFormatter() {
        return TemplateContants.TIME_FORMATTER;
    }
}
