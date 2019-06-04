package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author lepdou 2017-09-07
 */
public class JsonChecker extends TextChecker {

    public JsonChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected void doCheck(String value) {
        try {
            new ObjectMapper().readTree(value);
        } catch (Exception e) {
             throw new IllegalArgumentException(String.format("Json格式错误，请检查。 Key = %s", name()));
        }
        super.doCheck(value);
    }
}
