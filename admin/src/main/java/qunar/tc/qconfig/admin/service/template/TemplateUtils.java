package qunar.tc.qconfig.admin.service.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.joda.time.format.DateTimeFormatter;
import qunar.tc.qconfig.admin.service.impl.FileTemplateServiceImpl;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhenyu.nie created on 2016 2016/10/11 17:28
 */
public class TemplateUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static Optional<String> processTimeLongToStr(String dataId, String dataStr, String detailStr) {
        if (!FileChecker.isTemplateFile(dataId) || Strings.isNullOrEmpty(dataStr)) {
            return Optional.empty();
        }

        try {
            ObjectNode detail = (ObjectNode) mapper.readTree(detailStr);
            JsonNode data = mapper.readTree(dataStr);
            return processTimeLongToStr(dataId, data, detail);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("解析模版与文件时失败");
        }
    }

    // 先就这样吧，有需要再重构下这两个函数
    public static Optional<String> processTimeLongToStr(String dataId, JsonNode data, ObjectNode detail) {
        if (!FileChecker.isTemplateFile(dataId)) {
            return Optional.empty();
        }

        try {
            Map<String, String> nameTypeMapping = Maps.newHashMap();
            Map<String, ObjectNode> nameDetailMapping = Maps.newHashMap();
            getMapping(detail, nameTypeMapping, nameDetailMapping);
            Iterator<JsonNode> elements = data.elements();
            boolean edit = false;
            while (elements.hasNext()) {
                JsonNode row = elements.next();
                Iterator<Map.Entry<String, JsonNode>> fields = row.get(Constants.COLUMNS).fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String type = nameTypeMapping.get(field.getKey());
                    JsonNode value = field.getValue();
                    String valueStr = value.asText();

                    if (!Strings.isNullOrEmpty(valueStr) && (isTimeType(type) || isDateType(type))) {
                        edit = true;
                        Preconditions.checkArgument(value instanceof LongNode, "解析模版与文件时失败");
                        if (isTimeType(type)) {
                            field.setValue(new TextNode(TemplateContants.TIME_FORMATTER.print(value.longValue())));
                        } else if (isDateType(type)) {
                            field.setValue(new TextNode(TemplateContants.DATE_FORMATTER.print(value.longValue())));
                        }
                    }
                }
            }

            if (edit) {
                return Optional.of(mapper.writeValueAsString(data));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("解析模版与文件时失败");
        }
    }


    static void getMapping(ObjectNode detail, Map<String, String> nameTypeMapping, Map<String, ObjectNode> nameDetailMapping) {
        JsonNode columnsDetail = detail.get(Constants.COLUMNS);
        Iterator<JsonNode> columnsDetailIterator = columnsDetail.elements();
        while (columnsDetailIterator.hasNext()) {
            JsonNode columnDetail = columnsDetailIterator.next();
            nameTypeMapping.put(columnDetail.get("name").asText(), columnDetail.get("type").asText());
            nameDetailMapping.put(columnDetail.get("name").asText(), (ObjectNode) columnDetail);
        }
    }

    public static Optional<String> processTimeStrToLong(JsonNode data, ObjectNode detail) {
        try {
            Map<String, String> nameTypeMapping = Maps.newHashMap();
            Map<String, ObjectNode> nameDetailMapping = Maps.newHashMap();
            getMapping(detail, nameTypeMapping, nameDetailMapping);
            Iterator<JsonNode> elements = data.elements();
            boolean edit = false;
            while (elements.hasNext()) {
                JsonNode row = elements.next();
                Iterator<Map.Entry<String, JsonNode>> fields = row.get(Constants.COLUMNS).fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String type = nameTypeMapping.get(field.getKey());
                    JsonNode value = field.getValue();
                    String valueStr = value.asText();

                    if (!Strings.isNullOrEmpty(valueStr) && (isTimeType(type) || isDateType(type))) {
                        edit = true;
                        if (isTimeType(type)) {
                            field.setValue(new LongNode(parseDataTime(TemplateContants.TIME_FORMATTER, valueStr)));
                        } else if (isDateType(type)) {
                            field.setValue(new LongNode(parseDataTime(TemplateContants.DATE_FORMATTER, valueStr)));
                        }
                    }
                }
            }

            if (edit) {
                return Optional.of(mapper.writeValueAsString(data));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("解析模版与文件时失败");
        }
    }

    // 兼容时间戳格式
    private static long parseDataTime(DateTimeFormatter dataTimeFormatter, String valueStr) {
        try {
            return Long.parseLong(valueStr);
        } catch (Exception e) {
            return dataTimeFormatter.parseMillis(valueStr);
        }
    }

    public static boolean isTimeType(String type) {
        return TemplateContants.TIME_TYPE.equals(type);
    }

    public static boolean isDateType(String type) {
        return TemplateContants.DATE_TYPE.equals(type);
    }
}
