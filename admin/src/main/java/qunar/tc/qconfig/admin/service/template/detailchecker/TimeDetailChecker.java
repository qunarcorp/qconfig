package qunar.tc.qconfig.admin.service.template.detailchecker;

import org.joda.time.format.DateTimeFormatter;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

/**
 * @author zhenyu.nie created on 2016 2016/10/11 12:32
 */
public class TimeDetailChecker extends AbstractTimeRelativeDetailChecker {

    public TimeDetailChecker(String name) {
        super(name);
    }

    @Override
    protected DateTimeFormatter getFormatter() {
        return TemplateContants.TIME_FORMATTER;
    }
}
