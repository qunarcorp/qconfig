package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/4/11 12:36
 */
public class BooleanDetailChecker extends AbstractColumnDetailChecker {

    private static Set<String> minAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME);

    private static Set<String> maxAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME,
            TemplateContants.DESCRIPTION, TemplateContants.NULLABLE, TemplateContants.DEFAULT, TemplateContants.READONLY, TemplateContants.INHERITABLE);

    private final String name;

    public BooleanDetailChecker(String name) {
        this.name = name;
    }

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

    private static Context CONTEXT = new Context() {
        @Override
        public boolean isLegalValue(String value) {
            return "true".equals(value) || "false".equals(value);
        }
    };
}
