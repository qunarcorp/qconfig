package qunar.tc.qconfig.admin.service.template.detailchecker;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 17:50
 */
public class FloatDetailChecker extends AbstractNumberDetailChecker<Float> {

    public FloatDetailChecker(String name) {
        super(name);
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
