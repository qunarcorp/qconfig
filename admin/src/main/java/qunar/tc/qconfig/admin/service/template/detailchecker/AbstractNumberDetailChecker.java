package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 17:37
 */
public abstract class AbstractNumberDetailChecker<T extends Number> extends AbstractColumnDetailChecker {

    private static Set<String> minAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME);

    private static Set<String> maxAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME,
            TemplateContants.DESCRIPTION, TemplateContants.MIN, TemplateContants.MAX, TemplateContants.NULLABLE,
            TemplateContants.DEFAULT, TemplateContants.READONLY, TemplateContants.INHERITABLE);

    private String name;

    public AbstractNumberDetailChecker(String name) {
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

    protected abstract String type();

    protected abstract T minValue();

    protected abstract T maxValue();

    protected abstract T parse(String input);

    protected abstract boolean lq(T lhs, T rhs);

    @Override
    protected Context doCheck(ObjectNode node) {
        final T min = checkInt(node, "min", type(), minValue());
        final T max = checkInt(node, "max", type(), maxValue());

        Preconditions.checkArgument(lq(min, max), "[%s]的min[%s]必须小于等于max[%s]", name, min, max);
        return new Context() {
            @Override
            public boolean isLegalValue(String value) {
                try {
                    T typedValue = parse(value);
                    return lq(min, typedValue) && lq(typedValue, max);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
    }

    private T checkInt(ObjectNode object, String type, String typeText, T defaultValue) {
        JsonNode node = object.get(type);
        if (node == null) {
            return defaultValue;
        }
        Preconditions.checkArgument(node.isTextual(), "[%s]的[%s][%s]必须是个%s", name, type, node.asText(), typeText);
        if (!Strings.isNullOrEmpty(node.asText())) {
            try {
                return parse(node.asText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("[" + name + "]的" + type + "[" + node.asText() + "]必须是个" + typeText);
            }
        }
        return defaultValue;
    }
}
