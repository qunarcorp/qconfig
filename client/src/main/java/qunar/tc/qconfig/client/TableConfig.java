package qunar.tc.qconfig.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import qunar.tc.qconfig.client.impl.AbstractConfiguration;
import qunar.tc.qconfig.client.impl.ConfigEngine;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/4/8 10:40
 */
public class TableConfig extends AbstractConfiguration<QTable> {

    private static final ObjectMapper mapper = new ObjectMapper();

    private TableConfig(Feature feature, String fileName) {
        super(feature, fileName);
    }

    public static TableConfig get(String groupName, String fileName, Feature feature) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "fileName必须提供");
        Preconditions.checkArgument(FileChecker.isTemplateFile(fileName), "fileName必须是以.t结尾的模版类型文件");
        return (TableConfig) loader.load(groupName, fileName, feature, gen);
    }

    public static TableConfig get(String fileName, Feature feature) {
        return get(null, fileName, feature);
    }

    public static TableConfig get(String fileName) {
        return get(null, fileName, Feature.DEFAULT);
    }

    private static final QTable EMPTY_TABLE = new DefaultQTable(ImmutableTable.<String, String, String>of());

    @Override
    public QTable emptyData() {
        return EMPTY_TABLE;
    }

    @Override
    public QTable parse(String data) throws IOException {
        TableParser parser = getParser(feature.isTrimValue());
        return new DefaultQTable(parser.parse(data));
    }

    public static TableParser getParser(boolean trimValue) {
        return trimValue ? TRIM_PARSER : NOT_TRIM_PARSER;
    }

    private static final DataLoader loader = ConfigEngine.getInstance();

    private static final DataLoader.Generator gen = new DataLoader.Generator<QTable>() {
        @Override
        public AbstractConfiguration<QTable> create(Feature feature, String fileName) {
            return new TableConfig(feature, fileName);
        }
    };

    //用户拿到这个引用，每次配置变更，这个引用里面的数据也会变
    private final DynamicQTable ref = new DynamicQTable(current);

    /**
     * 每次配置变更，该table里的数据就会变化，持有该table引用每次取会取到最新值
     *
     * @return
     */
    public QTable asTable() {
        waitFistLoad();
        return ref;
    }

    public static final TableParser TRIM_PARSER = new TableParser(true);

    public static final TableParser NOT_TRIM_PARSER = new TableParser(false);

    public static class TableParser implements Parser<Table<String, String, String>> {

        private final boolean trimValue;

        public TableParser(boolean trimValue) {
            this.trimValue = trimValue;
        }

        @Override
        public Table<String, String, String> parse(String data) throws IOException {
            JsonNode jsonNode = mapper.readTree(data);

            ImmutableTable.Builder<String, String, String> builder = ImmutableTable.builder();
            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                JsonNode rowNode = elements.next();
                String row = rowNode.get(Constants.ROW).asText();
                Iterator<Map.Entry<String, JsonNode>> fields = rowNode.get(Constants.COLUMNS).fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    JsonNode valueNode = field.getValue();
                    if (valueNode != null && valueNode.asText() != null) {
                        String column = field.getKey();
                        String value = valueNode.asText();
                        if (trimValue) {
                            value = value.trim();
                        }
                        if (!Strings.isNullOrEmpty(value)) {
                            builder.put(row, column, value);
                        }
                    }
                }
            }
            return builder.build();
        }
    }
}
