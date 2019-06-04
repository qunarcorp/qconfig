package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import qunar.tc.qconfig.admin.service.template.TemplateContants;

import java.util.Iterator;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016/3/29 16:18
 */
public abstract class AbstractColumnDetailChecker implements ColumnDetailChecker {

    @Override
    public void check(ObjectNode node) {
        Iterator<String> iterator = node.fieldNames();
        ImmutableSet<String> attributes = ImmutableSet.copyOf(iterator);

        Set<String> minAttributes = getMinAttributes();
        Sets.SetView<String> require = Sets.difference(minAttributes, attributes);
        Preconditions.checkArgument(require.isEmpty(), "[%s]还缺少属性%s", name(), require);

        Set<String> maxAttributes = getMaxAttributes();
        Sets.SetView<String> illegal = Sets.difference(attributes, maxAttributes);
        Preconditions.checkArgument(illegal.isEmpty(), "[%s]包含无效属性%s", name(), illegal);

        Context context = doCheck(node);
        JsonNode defaultNode = node.get(TemplateContants.DEFAULT);
        if (defaultNode != null) {
            Preconditions.checkArgument(defaultNode.isTextual(), "default必须是一个文本属性");
            String defaultText = defaultNode.asText();
            Preconditions.checkArgument(defaultText.trim().length() == defaultText.length(), "[%s]默认值前后不能有空格", name());
            if (!Strings.isNullOrEmpty(defaultText)) {
                Preconditions.checkArgument(context.isLegalValue(defaultText), "[%s]默认值[%s]无效", name(), defaultText);
            }
        }
        JsonNode readonlyNode = node.get(TemplateContants.READONLY);
        if (readonlyNode != null) {
            Preconditions.checkArgument(readonlyNode.isBoolean(), "readonly必须是一个布尔属性");
            if (readonlyNode.asBoolean()) {
                Preconditions.checkArgument(defaultNode != null && !Strings.isNullOrEmpty(defaultNode.asText()), "只读只在有默认值时生效");
            }
        }
    }

    protected abstract Set<String> getMinAttributes();

    protected abstract Set<String> getMaxAttributes();

    protected abstract Context doCheck(ObjectNode node);
}
