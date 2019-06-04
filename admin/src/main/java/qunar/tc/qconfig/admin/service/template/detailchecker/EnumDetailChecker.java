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
 * @author zhenyu.nie created on 2016/3/29 16:48
 */
public class EnumDetailChecker extends AbstractColumnDetailChecker {

    private static final Set<String> minAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME, TemplateContants.VALUES);

    private static final Set<String> maxAttributes = ImmutableSet.of(TemplateContants.TYPE, TemplateContants.NAME,
            TemplateContants.DESCRIPTION, TemplateContants.VALUES, TemplateContants.NULLABLE, TemplateContants.DEFAULT, TemplateContants.READONLY, TemplateContants.INHERITABLE);

    private static final Set<String> NAME_WITH_DESC = ImmutableSet.of(TemplateContants.NAME, TemplateContants.DESCRIPTION);

    private final String name;

    public EnumDetailChecker(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    protected Set<String> getMinAttributes() {
        return minAttributes;
    }

    @Override
    protected Set<String> getMaxAttributes() {
        return maxAttributes;
    }

    @Override
    protected Context doCheck(ObjectNode node) {
        JsonNode values = node.get("values");
        Preconditions.checkArgument(values != null, "[%s]中没有枚举候选值", name);
        Preconditions.checkArgument(values.isArray(), "[%s]中没有枚举候选值", name);

        final Set<String> enumKeys = Sets.newHashSet();

        Iterator<JsonNode> elements = values.elements();
        if (!elements.hasNext()) {
            throw new IllegalArgumentException("[" + name + "]中至少应该有一个枚举值");
        }

        while (elements.hasNext()) {
            JsonNode next = elements.next();
            Preconditions.checkArgument(next.isObject(), "[%s]中枚举值信息错误", name);

            ImmutableSet<String> keys = ImmutableSet.copyOf(next.fieldNames());
            Sets.SetView<String> difference = Sets.difference(keys, NAME_WITH_DESC);
            if (!difference.isEmpty()) {
                throw new IllegalArgumentException("非法的枚举值属性" + ImmutableSet.copyOf(difference));
            }

            String enumItemName = checkName(next.get(TemplateContants.NAME));
            if (!enumKeys.add(enumItemName)) {
                throw new IllegalArgumentException("[" + name + "]中重复的枚举值名[" + enumItemName + "]");
            }

            JsonNode desc = next.get(TemplateContants.DESCRIPTION);
            if (desc != null) {
                if (!desc.isTextual() || desc.asText().length() != desc.asText().trim().length()) {
                    throw new IllegalArgumentException("[" + name + "]中枚举[" + enumItemName + "]非法的描述");
                }
            }
        }

        return new Context() {
            @Override
            public boolean isLegalValue(String value) {
                return enumKeys.contains(value);
            }
        };
    }

    private static String checkName(JsonNode name) {
        if (name == null || !name.isTextual() || Strings.isNullOrEmpty(name.asText()) || name.asText().length() != name.asText().trim().length()) {
            String nameText = name == null ? "" : name.asText();
            throw new IllegalArgumentException("非法的枚举值名[" + nameText + "]");
        }
        return name.asText();
    }
}
