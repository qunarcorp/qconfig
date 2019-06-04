package qunar.tc.qconfig.admin.service.template.detailchecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 17:48
 */
public class LongDetailChecker extends AbstractNumberDetailChecker<Long> {

    public LongDetailChecker(String name) {
        super(name);
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
