package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.joda.time.format.DateTimeFormatter;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/10/12 11:55
 */
public abstract class AbstractTimeRelativeDetailChecker extends AbstractColumnDetailChecker {

    private static Set<String> minAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME);

    private static Set<String> maxAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME,
            TemplateContants.DESCRIPTION, TemplateContants.NULLABLE, TemplateContants.DEFAULT, TemplateContants.READONLY, TemplateContants.INHERITABLE);

    private final String name;

    public AbstractTimeRelativeDetailChecker(String name) {
        this.name = name;
    }

    protected abstract DateTimeFormatter getFormatter();

    @Override
    public String name() {
        return name;
    }

    @Override
    protected Set<String> getMinAttributes() {
        return minAttributes;
    }

    @Override
    protected Set<String> getMaxAttributes() {
        return maxAttributes;
    }

    @Override
    protected Context doCheck(ObjectNode node) {
        return CONTEXT;
    }

    private final Context CONTEXT = new Context() {
        @Override
        public boolean isLegalValue(String value) {
            try {
                getFormatter().parseDateTime(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };
}
