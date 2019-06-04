package qunar.tc.qconfig.admin.service.template.detailchecker;

import org.joda.time.format.DateTimeFormatter;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

/**
 * @author zhenyu.nie created on 2016 2016/10/12 11:54
 */
public class DateDetailChecker extends AbstractTimeRelativeDetailChecker {

    public DateDetailChecker(String name) {
        super(name);
    }

    @Override
    protected DateTimeFormatter getFormatter() {
        return TemplateContants.DATE_FORMATTER;
    }
}
