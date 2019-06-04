package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

/**
 * Created by pingyang.yang on 2019-01-04
 */
public class RegularChecker extends NullableChecker {

    private String regularString;

    public RegularChecker(ObjectNode node) {
        super(node);
        this.regularString = node.get("regular").asText();
    }

    @Override
    protected void doCheck(String value) {
        if (!Strings.isNullOrEmpty(regularString) && !value.matches(regularString)) {
            throw new IllegalArgumentException(String.format("正则表示式校验不通过,正则为[%s]，value为[%s]", regularString, value));
        }
    }
}
