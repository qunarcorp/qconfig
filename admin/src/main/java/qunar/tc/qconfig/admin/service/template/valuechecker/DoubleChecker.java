package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 18:04
 */
public class DoubleChecker extends AbstractNumberValueChecker<Double> {

    public DoubleChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected String type() {
        return "double";
    }

    @Override
    protected Double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    protected Double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    protected Double parse(String input) {
        return Double.parseDouble(input);
    }

    @Override
    protected boolean lq(Double lhs, Double rhs) {
        return lhs <= rhs;
    }
}
