package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zhenyu.nie created on 2016/3/29 14:53
 */
public class TextChecker extends NullableChecker {

    public TextChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected void doCheck(String value) {

    }
}
