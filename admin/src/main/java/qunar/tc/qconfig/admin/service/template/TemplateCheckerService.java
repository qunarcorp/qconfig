package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.template.detailchecker.ColumnDetailChecker;
import qunar.tc.qconfig.admin.service.template.detailchecker.RowDetailChecker;
import qunar.tc.qconfig.admin.service.template.exception.ValueCheckException;
import qunar.tc.qconfig.admin.service.template.valuechecker.AbstractColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.ColumnValueChecker;
import qunar.tc.qconfig.admin.service.template.valuechecker.RowValueChecker;
import qunar.tc.qconfig.common.util.Constants;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.UnexpectedException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016/3/29 16:04
 */
@Service
public class TemplateCheckerService {

    private static final ObjectMapper mapper = TemplateUtils.getMapper();

    @Resource
    private List<TemplateCheckerFactory> checkerFactories;

    @Resource
    private RowDetailChecker rowDetailChecker;

    private Map<String, TemplateCheckerFactory> checkerFactoryMap = Maps.newHashMap();

    @PostConstruct
    public void init() {
        for (TemplateCheckerFactory factory : checkerFactories) {
            checkerFactoryMap.put(factory.type(), factory);
        }
    }

    public void checkPropertiesDetail(String detail) {
        //tole 兼容前端传空模板[]或{}
        if (Strings.isNullOrEmpty(detail) || detail.equals("[]")) {
            return;
        }

        JsonNode columns;
        try {
            columns = mapper.readTree(detail);
        } catch (Exception e) {
            throw new RuntimeException("读取错误, 输入应该为一个合格的json对象");
        }

        Preconditions
                .checkArgument(columns != null && columns.isArray() && columns.size() > 0, "properties模版列表必须是一个不为空的数组");
        Set<String> names = Sets.newHashSet();
        for (JsonNode columnNode : columns) {
            Preconditions.checkArgument(columnNode.isObject(), "property模版必须是json对象");
            JsonNode nameNode = columnNode.get(TemplateContants.NAME);
            Preconditions.checkArgument(nameNode != null && nameNode.isTextual(), "property模版必须有name的文本属性");
            String name = nameNode.asText();

            Preconditions.checkArgument(names.add(name), "[%s]key重复", name);
            Preconditions.checkArgument(name.trim().length() == name.length(), "[%s]key前后不能有空格", name);

            JsonNode typeNode = columnNode.get(TemplateContants.TYPE);

            Preconditions.checkArgument(
                    typeNode != null && typeNode.isTextual() && checkerFactoryMap.keySet().contains(typeNode.asText()),
                    "property模版[%s]必须有type属性，并且为boolean, int, enum, text中一个", name);

            TemplateCheckerFactory checkerFactory = checkerFactoryMap.get(typeNode.asText());
            ColumnDetailChecker detailChecker = checkerFactory.createDetailChecker(name);
            detailChecker.check((ObjectNode) columnNode);
        }
    }

    public void checkDetail(String detail) {
        ObjectNode node;
        try {
            node = (ObjectNode) mapper.readTree(detail);
        } catch (Exception e) {
            throw new RuntimeException("读取错误, 输入应该为一个合格的json对象");
        }

        JsonNode rowsNode = node.get(Constants.ROWS);
        if (rowDetailChecker != null) {
            rowDetailChecker.check(rowsNode);
        }

        JsonNode columns = node.get(Constants.COLUMNS);
        Preconditions.checkArgument(columns != null && columns.isArray() && columns.size() > 0, "columns列表必须是一个不为空的数组");
        Set<String> names = Sets.newHashSet();
        Set<String> descriptions = Sets.newHashSet();
        for (JsonNode columnNode : columns) {
            Preconditions.checkArgument(columnNode.isObject(), "columns列表中必须是列描述json对象");
            JsonNode nameNode = columnNode.get(TemplateContants.NAME);
            Preconditions.checkArgument(nameNode != null && nameNode.isTextual(), "column必须有name的文本属性");
            String name = nameNode.asText();

            Preconditions.checkArgument(!name.contains(Constants.ROW_COLUMN_SEPARATOR), "列名不能有斜杠, [%s]", name);
            Preconditions.checkArgument(names.add(name), "[%s]列名重复", name);
            Preconditions.checkArgument(name.trim().length() == name.length(), "[%s]列名前后不能有空格", name);

            Preconditions.checkArgument(rowsNode != null || !Strings.isNullOrEmpty(name), "单行类型模版列名不能为空");

            JsonNode descriptionNode = columnNode.get(TemplateContants.DESCRIPTION);
            Preconditions.checkArgument(descriptionNode != null && descriptionNode.isTextual()
                            && descriptionNode.asText().length() == descriptionNode.asText().trim().length(), "列[%s]非法的列描述",
                    name);
            String description = descriptionNode.asText();
            if (!Strings.isNullOrEmpty(description)) {
                Preconditions.checkArgument(descriptions.add(description), "列[%s]描述重复", name);
            }

            JsonNode typeNode = columnNode.get(TemplateContants.TYPE);

            Preconditions.checkArgument(
                    typeNode != null && typeNode.isTextual() && checkerFactoryMap.keySet().contains(typeNode.asText()),
                    "列[%s]必须有type属性，并且为boolean, int, enum, text中一个", name);

            TemplateCheckerFactory checkerFactory = checkerFactoryMap.get(typeNode.asText());
            ColumnDetailChecker detailChecker = checkerFactory.createDetailChecker(name);
            detailChecker.check((ObjectNode) columnNode);
        }
    }

    public void checkInheritableProperties(String data, Map<String, ObjectNode> nameDetailMapping,
            Map<String, String> nameTypeMapping, Map<String, ObjectNode> parentNameDetailMapping,
            Map<String, String> parentNameTypeMapping) {
        try {
            Properties p = new Properties();
            p.load(new StringReader(data));
            for (String name : nameTypeMapping.keySet()) {
                if (p.containsKey(name)) {
                    Map.Entry<String, String> entry = Maps.immutableEntry(name, (String) p.get(name));
                    if (!parentNameTypeMapping.containsKey(name) && !parentNameDetailMapping.containsKey(name)) {
                        checkValue(nameTypeMapping, nameDetailMapping, entry, true);
                    }
                }

            }

            for (String name : parentNameTypeMapping.keySet()) {
                if (p.containsKey(name)) {
                    Map.Entry<String, String> entry = Maps.immutableEntry(name, (String) p.get(name));
                    if (parentNameTypeMapping.containsKey(name) && parentNameDetailMapping.containsKey(name)) {
                        checkValue(parentNameTypeMapping, parentNameDetailMapping, entry, true);
                    }
                }
            }
        } catch (IOException e) {
            // not happen
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new ValueCheckException(e.getMessage());
        }
    }

    public void checkProperties(String data, Map<String, ObjectNode> nameDetailMapping,
            Map<String, String> nameTypeMapping) {
        try {
            Properties p = new Properties();
            p.load(new StringReader(data));
            for (String name : nameTypeMapping.keySet()) {
                Map.Entry<String, String> entry = Maps.immutableEntry(name, (String) p.get(name));
                checkValue(nameTypeMapping, nameDetailMapping, entry, true);//这部分校验，包含了非空和枚举内容校验
            }
        } catch (IOException e) {
            // not happen
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new ValueCheckException(e.getMessage());
        }
    }

    /**
     * 检查父配置是否可继承
     */
    public void checkInheritConstraint(String data, Map<String, ObjectNode> nameDetailMapping,
            Map<String, String> nameTypeMapping) {
        try {
            Properties p = new Properties();
            p.load(new StringReader(data));

            for (Map.Entry<String, String> nameType : nameTypeMapping.entrySet()) {
                if (!p.containsKey(nameType.getKey())) {
                    continue;
                }
                TemplateCheckerFactory checkerFactory = checkerFactoryMap.get(nameType.getValue());
                ColumnValueChecker valueChecker = checkerFactory
                        .createValueChecker(nameDetailMapping.get(nameType.getKey()));
                AbstractColumnValueChecker inheritableChecker;
                if (valueChecker instanceof AbstractColumnValueChecker) {
                    inheritableChecker = (AbstractColumnValueChecker) valueChecker;
                    Preconditions.checkArgument(inheritableChecker.isInhertiable(), "[%s]里的值不能被继承", nameType.getKey());
                } else {
                    throw new UnexpectedException("校验出现异常，请联系管理员！");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new ValueCheckException(e.getMessage());
        }
    }

    public Optional<String> processValue(String data, String templateDetail) {
        try {
            RowValueChecker rowValueChecker = new RowValueChecker();
            ObjectNode detail = (ObjectNode) mapper.readTree(templateDetail);
            Map<String, String> nameTypeMapping = Maps.newHashMap();
            Map<String, ObjectNode> nameDetailMapping = Maps.newHashMap();
            TemplateUtils.getMapping(detail, nameTypeMapping, nameDetailMapping);

            JsonNode jsonNode = mapper.readTree(data);

            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                JsonNode row = elements.next();
                String rowName = row.get(Constants.ROW).asText();
                rowValueChecker.check(rowName);

                Iterator<Map.Entry<String, JsonNode>> fields = row.get(Constants.COLUMNS).fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    checkValue(nameTypeMapping, nameDetailMapping,
                            Maps.immutableEntry(field.getKey(), field.getValue().asText()), true);
                }
            }

            return TemplateUtils.processTimeStrToLong(jsonNode, detail);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new ValueCheckException(e.getMessage());
        }
    }

    private void checkValue(Map<String, String> nameTypeMapping, Map<String, ObjectNode> nameDetailMapping,
            Map.Entry<String, String> field, boolean checkNullable) {
        String type = nameTypeMapping.get(field.getKey());
        if (!Strings.isNullOrEmpty(type)) {
            TemplateCheckerFactory checkerFactory = checkerFactoryMap.get(type);
            ColumnValueChecker valueChecker = checkerFactory.createValueChecker(nameDetailMapping.get(field.getKey()));
            String valueStr = field.getValue();
            if (checkNullable) {
                valueChecker.check(valueStr);
            } else {
                valueChecker.checkWithoutNullable(valueStr);
            }
        }
    }

    public void checkDefaultConfig(String template, String defaultConfig) {
        try {
            ObjectNode templateNode = (ObjectNode) mapper.readTree(template);
            ObjectNode configNode = (ObjectNode) mapper.readTree(defaultConfig);
            Map<String, String> nameTypeMapping = Maps.newHashMap();
            Map<String, ObjectNode> nameDetailMapping = Maps.newHashMap();
            TemplateUtils.getMapping(templateNode, nameTypeMapping, nameDetailMapping);

            Iterator<Map.Entry<String, JsonNode>> fields = configNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().get(TemplateContants.READONLY).asBoolean()) {
                    String defaultText = field.getValue().get(TemplateContants.DEFAULT).asText();
                    Preconditions.checkArgument(!Strings.isNullOrEmpty(defaultText), "只读只在有默认值时生效");
                }
                checkValue(nameTypeMapping, nameDetailMapping,
                        Maps.immutableEntry(field.getKey(), field.getValue().get(TemplateContants.DEFAULT).asText()),
                        false);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 处理模版与数据中间无法匹配的问题
     *
     * @param data           数据
     * @param templateDetail 模版
     * @return 处理后的数据
     */
    public String processDetailContent(String data, String templateDetail) {
        try {
            ObjectNode detail = (ObjectNode) mapper.readTree(templateDetail);
            Map<String, ObjectNode> nameDetailMapping = Maps.newHashMap();
            JsonNode columnsDetail = detail.get(Constants.COLUMNS);
            Iterator<JsonNode> columnsDetailIterator = columnsDetail.elements();
            while (columnsDetailIterator.hasNext()) {
                JsonNode columnDetail = columnsDetailIterator.next();
                nameDetailMapping.put(columnDetail.get("name").asText(), (ObjectNode) columnDetail);
            }

            JsonNode jsonNode = mapper.readTree(data);

            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                JsonNode row = elements.next();

                Iterator<Map.Entry<String, JsonNode>> fields = row.get(Constants.COLUMNS).fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    if (nameDetailMapping.get(field.getKey()) == null) {
                        fields.remove();
                    }
                }
            }
            return mapper.writeValueAsString(jsonNode);

        } catch (IOException e) {
            throw new IllegalArgumentException("data illegal", e);
        }
    }
}
