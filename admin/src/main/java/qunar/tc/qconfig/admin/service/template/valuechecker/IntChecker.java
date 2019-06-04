package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zhenyu.nie created on 2016/3/29 15:34
 */
public class IntChecker extends AbstractNumberValueChecker<Integer> {

    public IntChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected Integer parse(String input) {
        return Integer.parseInt(input);
    }

    @Override
    protected Integer minValue() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected Integer maxValue() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected String type() {
        return "int";
    }

    @Override
    protected boolean lq(Integer lhs, Integer rhs) {
        return lhs <= rhs;
    }
}
