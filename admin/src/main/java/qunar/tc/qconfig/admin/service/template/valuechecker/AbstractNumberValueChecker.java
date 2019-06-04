package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 17:54
 */
public abstract class AbstractNumberValueChecker<T extends Number> extends NullableChecker {

    private T min;

    private T max;

    public AbstractNumberValueChecker(ObjectNode node) {
        super(node);
        JsonNode minNode = node.get(TemplateContants.MIN);
        // tole 未指定min/max时取默认值
//        String min = minNode.asText();
        this.min = minValue();
        if (minNode != null && !Strings.isNullOrEmpty(minNode.asText())) {
            this.min = parse(minNode.asText());
        }
        JsonNode maxNode = node.get(TemplateContants.MAX);
//        String max = maxNode.asText();
        this.max = maxValue();
        if (maxNode != null && !Strings.isNullOrEmpty(maxNode.asText())) {
            this.max = parse(maxNode.asText());
        }
    }

    @Override
    protected void doCheck(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }

        T intValue;
        try {
            intValue = parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(name() + "的值[" + value + "]必须是" + type() + "类型");
        }
        Preconditions.checkArgument(lq(min, intValue), "[%s]的值[%s]必须大于等于[%s]", name(), intValue, min);
        Preconditions.checkArgument(lq(intValue, max), "[%s]的值[%s]必须小于等于[%s]", name(), intValue, max);
    }

    protected abstract T parse(String input);

    protected abstract T minValue();

    protected abstract T maxValue();

    protected abstract String type();

    protected abstract boolean lq(T lhs, T rhs);
}
