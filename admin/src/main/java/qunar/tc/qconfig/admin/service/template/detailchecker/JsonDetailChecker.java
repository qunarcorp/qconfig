package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonParser;

/**
 * @author lepdou 2017-09-07
 */
public class JsonDetailChecker extends TextDetailChecker {
    private static final JsonParser jsonParser = new JsonParser();

    public JsonDetailChecker(String name) {
        super(name);
    }

    @Override
    protected Context doCheck(ObjectNode node) {
        return CONTEXT;
    }

    private final Context CONTEXT = new Context() {
        @Override
        public boolean isLegalValue(String value) {
            try {
                jsonParser.parse(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };
}
