package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

/**
 * @author zhenyu.nie created on 2016/3/29 15:31
 */
public abstract class AbstractColumnValueChecker implements ColumnValueChecker {

    private String name;

    private boolean nullable;

    private boolean inhertiable;

    public AbstractColumnValueChecker(ObjectNode node) {
        this.name = node.get(TemplateContants.NAME).asText();
        this.nullable = isNullable(node.get(TemplateContants.NULLABLE));
        this.inhertiable = isInhertiable(node.get(TemplateContants.INHERITABLE));
    }

    public String name() {
        return name;
    }

    private boolean isNullable(JsonNode node) {
        return  node2NullableBoolean(node);
    }

    public boolean isNullable() {
        return nullable;
    }

    private boolean isInhertiable(JsonNode node) {
        return  node2InheritBoolean(node);
    }

    public  boolean isInhertiable() {
        return inhertiable;
    }

    protected boolean node2Boolean(JsonNode node, boolean defaultValue) {
        if (node == null) {
            return defaultValue;
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isTextual()) {
            return Boolean.parseBoolean(node.asText());
        } else {
            return false;
        }
    }

    protected boolean node2InheritBoolean(JsonNode node) {
       return node2Boolean(node, true);
}

    protected boolean node2NullableBoolean(JsonNode node) {
        return node2Boolean(node, false);
    }
}
