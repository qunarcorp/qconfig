package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Iterator;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016/3/29 15:16
 */
public class EnumChecker extends AbstractColumnValueChecker {

    private Set<String> enums = Sets.newHashSet();

    public EnumChecker(ObjectNode node) {
        super(node);

        if (isNullable()) {
            enums.add("");
        }

        ArrayNode values = (ArrayNode) node.get(TemplateContants.VALUES);
        Iterator<JsonNode> iterator = values.elements();
        while (iterator.hasNext()) {
            JsonNode next = iterator.next();
            if (next.isTextual()) {
                enums.add(next.asText());
            } else {
                enums.add(next.get(TemplateContants.NAME).asText());
            }
        }

    }

    @Override
    public void check(String value) {
        Preconditions.checkArgument(enums.contains(Strings.nullToEmpty(value)), "[%s]不是[%s]枚举中的值", value, name());
    }

    @Override
    public void checkWithoutNullable(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        check(value);
    }
}
