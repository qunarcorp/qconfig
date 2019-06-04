package qunar.tc.qconfig.admin.service.template.detailchecker;

/**
 * @author zhenyu.nie created on 2016/3/29 16:35
 */
public class IntDetailChecker extends AbstractNumberDetailChecker<Integer> {

    public IntDetailChecker(String name) {
        super(name);
    }

    @Override
    protected String type() {
        return "int";
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
    protected Integer parse(String input) {
        return Integer.parseInt(input);
    }

    @Override
    protected boolean lq(Integer lhs, Integer rhs) {
        return lhs <= rhs;
    }
}
