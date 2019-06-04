package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 18:02
 */
public class LongChecker extends AbstractNumberValueChecker<Long> {
    public LongChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected String type() {
        return "long";
    }

    @Override
    protected Long minValue() {
        return Long.MIN_VALUE;
    }

    @Override
    protected Long maxValue() {
        return Long.MAX_VALUE;
    }

    @Override
    protected Long parse(String input) {
        return Long.parseLong(input);
    }

    @Override
    protected boolean lq(Long lhs, Long rhs) {
        return lhs <= rhs;
    }
}
