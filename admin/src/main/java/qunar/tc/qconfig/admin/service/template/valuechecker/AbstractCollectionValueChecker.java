package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Collection;

public abstract class AbstractCollectionValueChecker<T> extends NullableChecker {

    private int minSize;
    private int maxSize;

    public AbstractCollectionValueChecker(ObjectNode node) {
        super(node);
        this.minSize = getSize(node, TemplateContants.MIN, 0);
        this.maxSize = getSize(node, TemplateContants.MAX, Integer.MAX_VALUE);
    }

    @Override
    protected void doCheck(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        Collection<T> elements = null;
        try {
            elements = parseElements(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        int size = elements.size();
        if (size < minSize || size > maxSize) {
            throw new IllegalArgumentException(String.format("size必须在%s到%s范围内", minSize, maxSize));
        }
    }

    protected abstract Collection<T> parseElements(String value);

    private int getSize(ObjectNode jsonNode, String type, int defaultValue) {
        JsonNode sizeNode = jsonNode.get(type);
        if (sizeNode == null) {
            return defaultValue;
        }
        Preconditions.checkArgument(sizeNode.isTextual(), "[%s]的[%s][%s]必须是int", name(), type, sizeNode.asText());
        if (!Strings.isNullOrEmpty(sizeNode.asText())) {
            try {
                return Integer.valueOf(sizeNode.asText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("[" + name() + "]的" + type + "[" + sizeNode.asText() + "]必须是int");
            }
        }
        return defaultValue;
    }
}
