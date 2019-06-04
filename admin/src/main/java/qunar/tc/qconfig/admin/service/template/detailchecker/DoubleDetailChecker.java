package qunar.tc.qconfig.admin.service.template.detailchecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 17:52
 */
public class DoubleDetailChecker extends AbstractNumberDetailChecker<Double> {

    public DoubleDetailChecker(String name) {
        super(name);
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
