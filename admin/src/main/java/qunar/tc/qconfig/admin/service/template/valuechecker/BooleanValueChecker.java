package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author zhenyu.nie created on 2016 2016/4/11 12:55
 */
public class BooleanValueChecker extends NullableChecker {

    public BooleanValueChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected void doCheck(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        Preconditions.checkArgument("true".equals(value) || "false".equals(value), "boolean类型值[%s]只能为true或者false", name());
    }
}
