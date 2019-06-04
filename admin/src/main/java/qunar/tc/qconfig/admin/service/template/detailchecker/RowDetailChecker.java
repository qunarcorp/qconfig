package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.TemplateContants;
import qunar.tc.qconfig.common.util.Constants;

import java.util.Iterator;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016/3/29 16:57
 */
@Service
public class RowDetailChecker {

    private static final Set<String> keys = ImmutableSet.of(TemplateContants.VALUES);

    public void check(JsonNode node) {
        Preconditions.checkArgument(node.isObject(), "rows应该为一个对象");
        ImmutableSet<String> fields = ImmutableSet.copyOf(node.fieldNames());
        Preconditions.checkArgument(Sets.difference(fields, keys).isEmpty(), "rows里只能包含如下属性: %s", keys);

        JsonNode values = node.get(TemplateContants.VALUES);

        Iterator<JsonNode> elements;
        Preconditions.checkArgument(values != null && values.isArray(), "没有候选行名");
        elements = values.elements();

        while (elements.hasNext()) {
            JsonNode next = elements.next();
            Preconditions.checkArgument(next.isTextual(), "行名只能是字符串");
            String row = next.asText();
            Preconditions.checkArgument(!row.contains(Constants.ROW_COLUMN_SEPARATOR), "行名不能有斜杠, [%s]", row);
            Preconditions.checkArgument(row.length() == row.trim().length(), "行名前后不能有空格, [%s]", row);
        }
    }
}
