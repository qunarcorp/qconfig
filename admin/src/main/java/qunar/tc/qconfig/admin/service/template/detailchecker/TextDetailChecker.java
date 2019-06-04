package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2016/3/29 16:17
 */
public class TextDetailChecker extends AbstractColumnDetailChecker {

    private static final Set<String> minAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME);

    private static final Set<String> maxAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME,
            TemplateContants.DESCRIPTION, TemplateContants.NULLABLE, TemplateContants.DEFAULT, TemplateContants.READONLY, TemplateContants.INHERITABLE, TemplateContants.REGULAR);

    private String name;

    public TextDetailChecker(String name) {
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
        return ALWAYS_TRUE_CONTEXT;
    }

    private static Context ALWAYS_TRUE_CONTEXT = new Context() {
        @Override
        public boolean isLegalValue(String value) {
            return true;
        }
    };
}
