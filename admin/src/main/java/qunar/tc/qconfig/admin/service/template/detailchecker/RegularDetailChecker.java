package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import java.util.Set;

/**
 * Created by pingyang.yang on 2019-01-04
 */
public class RegularDetailChecker extends TextDetailChecker {

    private String regularString;

    public RegularDetailChecker(String name) {
        super(name);
    }

    @Override
    protected Context doCheck(ObjectNode node) {
        regularString = node.get("regular").asText();
        return CONTEXT;
    }

    private final Context CONTEXT = new Context() {
        @Override
        public boolean isLegalValue(String value) {
            try {
                if (Strings.isNullOrEmpty(regularString)) return false;
                return value.matches(regularString);
            } catch (Exception e) {
                return false;
            }
        }
    };
}
