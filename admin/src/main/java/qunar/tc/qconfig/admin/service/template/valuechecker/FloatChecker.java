package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 18:03
 */
public class FloatChecker extends AbstractNumberValueChecker<Float> {

    public FloatChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected String type() {
        return "float";
    }

    @Override
    protected Float minValue() {
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    protected Float maxValue() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    protected Float parse(String input) {
        return Float.parseFloat(input);
    }

    @Override
    protected boolean lq(Float lhs, Float rhs) {
        return lhs <= rhs;
    }
}
