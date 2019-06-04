package qunar.tc.qconfig.admin.service.template;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author zhenyu.nie created on 2016/3/29 15:26
 */
public class TemplateContants {

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String NULLABLE = "nullable";

    public static final String INHERITABLE = "inheritable";

    public static final String VALUES = "values";

    public static final String TYPE = "type";

    public static final String MAX = "max";

    public static final String MIN = "min";

    public static final String SINGLE = "single";

    public static final String UNCERTAIN = "uncertain";

    public static final String FIXED = "fixed";

    public static final String DEFAULT = "default";

    public static final String READONLY = "isReadonly";

    public static final String REGULAR = "regular";

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static final String TIME_TYPE = "time";

    public static final String DATE_TYPE = "date";

}
