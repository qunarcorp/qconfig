package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Collection;
import java.util.Set;

public abstract class AbstractCollectionChecker<T> extends AbstractColumnDetailChecker {


    private static Set<String> minAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME);

    private static Set<String> maxAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME,
            TemplateContants.DESCRIPTION, TemplateContants.MIN, TemplateContants.MAX, TemplateContants.NULLABLE,
            TemplateContants.DEFAULT, TemplateContants.READONLY, TemplateContants.INHERITABLE);

    private String name;

    public AbstractCollectionChecker(String name) {
        this.name = name;
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
    protected Context doCheck(final ObjectNode node) {
        final int minSize = getSize(node, "min", 0);
        final int maxSize = getSize(node, "max", Integer.MAX_VALUE);
        Preconditions.checkArgument(minSize <= maxSize, "%s的min[%s]应小于或等于max[%s]", name, minSize, maxSize);
        return new Context() {
            @Override
            public boolean isLegalValue(String value) {
                Collection<T> elements = null;
                try {
                    elements = parseElements(value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
                int size = elements.size();
                if (size < minSize || size > maxSize) {
                    return false;
                }
                return true;
            }
        };
    }

    @Override
    public String name() {
        return name;
    }

    abstract protected Collection<T> parseElements(String value);

    private int getSize(ObjectNode jsonNode, String type, int defaultValue) {
        JsonNode sizeNode = jsonNode.get(type);
        if (sizeNode == null) {
            return defaultValue;
        }
        Preconditions.checkArgument(sizeNode.isTextual(), "[%s]的[%s][%s]必须是int", name, type, sizeNode.asText());
        if (!Strings.isNullOrEmpty(sizeNode.asText())) {
            try {
                return Integer.valueOf(sizeNode.asText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("[" + name + "]的" + type + "[" + sizeNode.asText() + "]必须是int");
            }
        }
        return defaultValue;
    }
}
