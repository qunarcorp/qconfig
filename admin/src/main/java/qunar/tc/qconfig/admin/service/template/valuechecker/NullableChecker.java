package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author zhenyu.nie created on 2016/3/29 15:04
 */
public abstract class NullableChecker extends AbstractColumnValueChecker {

    private boolean nullable;

    public NullableChecker(ObjectNode node) {
        super(node);
        nullable = isNullable();
    }

    @Override
    public final void check(String value) {
        if (!nullable) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(value), "[%s]里的值不能为空", name());
        }
        doCheck(value);
    }

    @Override
    public final void checkWithoutNullable(String value) {
        doCheck(value);
    }

    protected abstract void doCheck(String value);
}
