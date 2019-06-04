package qunar.tc.qconfig.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import qunar.tc.qconfig.client.impl.AbstractConfiguration;
import qunar.tc.qconfig.client.impl.ConfigEngine;
import qunar.tc.qconfig.client.impl.Translators;
import qunar.tc.qconfig.common.util.FileChecker;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: zhaohuiyu
 * Date: 5/21/14
 * Time: 2:33 PM
 */
public class TypedConfig<T> extends AbstractConfiguration<T> {

    private static final DataLoader loader = ConfigEngine.getInstance();

    private final Parser<T> parser;

    private TypedConfig(Parser<T> parser, Feature feature, String fileName) {
        super(feature, fileName);
        this.parser = parser;
    }

    public static <U> TypedConfig<U> get(String groupName, String fileName, Feature feature, Parser<U> parser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "file name should be provided");
        return (TypedConfig<U>) loader.load(groupName, fileName, feature, new Generator<>(parser));
    }

    public static <U> TypedConfig<U> get(String groupName, String fileName, Feature feature, Class<U> clazz) {
        Preconditions.checkArgument(clazz != null, "bean class should be provided");
        if (feature == null) {
            feature = Feature.DEFAULT;
        }
        return get(groupName, fileName, feature, new BeanParser<>(fileName, clazz, feature.isTrimValue()));
    }

    public static <U> TypedConfig<List<U>> getList(String groupName, String fileName, Feature feature, Class<U> clazz) {
        Preconditions.checkArgument(clazz != null, "bean class should be provided");
        Preconditions.checkArgument(FileChecker.isTemplateFile(fileName), "file name should be template file which end with .t");
        if (feature == null) {
            feature = Feature.DEFAULT;
        }
        return get(groupName, fileName, feature, new ListBeanParser<>(clazz, feature.isTrimValue()));
    }

    public static <U> TypedConfig<Map<String, U>> getMap(String groupName, String fileName, Feature feature, Class<U> clazz) {
        Preconditions.checkArgument(clazz != null, "bean class should be provided");
        Preconditions.checkArgument(FileChecker.isTemplateFile(fileName), "file name should be template file which end with .t");
        if (feature == null) {
            feature = Feature.DEFAULT;
        }
        return get(groupName, fileName, feature, new MapBeanParser<>(clazz, feature.isTrimValue()));
    }

    public static <U> TypedConfig<U> get(String fileName, Feature feature, Parser<U> parser) {
        return get(null, fileName, feature, parser);
    }

    public static <U> TypedConfig<U> get(String fileName, Feature feature, Class<U> clazz) {
        return get(null, fileName, feature, clazz);
    }

    public static <U> TypedConfig<List<U>> getList(String fileName, Feature feature, Class<U> clazz) {
        return getList(null, fileName, feature, clazz);
    }

    public static <U> TypedConfig<Map<String, U>> getMap(String fileName, Feature feature, Class<U> clazz) {
        return getMap(null, fileName, feature, clazz);
    }

    public static <U> TypedConfig<U> get(String fileName, Parser<U> parser) {
        return get(null, fileName, null, parser);
    }

    public static <U> TypedConfig<U> get(String fileName, Class<U> clazz) {
        return get(null, fileName, null, clazz);
    }

    public static <U> TypedConfig<List<U>> getList(String fileName, Class<U> clazz) {
        return getList(null, fileName, null, clazz);
    }

    public static <U> TypedConfig<Map<String, U>> getMap(String fileName, Class<U> clazz) {
        return getMap(null, fileName, null, clazz);
    }

    @Override
    public T emptyData() {
        return null;
    }

    @Override
    public T parse(String data) throws IOException {
        return parser.parse(data);
    }

    public T current() {
        waitFistLoad();
        return current.get();
    }

    private static final class Generator<U> implements DataLoader.Generator<U> {

        private Parser<U> parser;

        public Generator(Parser<U> parser) {
            this.parser = parser;
        }

        @Override
        public AbstractConfiguration<U> create(Feature feature, String fileName) {
            return new TypedConfig<U>(parser, feature, fileName);
        }
    }

    public interface Parser<T> {
        T parse(String data) throws IOException;
    }

    private static class BeanParser<U> implements Parser<U> {

        private final String fileName;

        private final Class<U> clazz;

        private final boolean isTrimValue;

        public BeanParser(String fileName, Class<U> clazz, boolean isTrimValue) {
            this.fileName = fileName;
            this.clazz = clazz;
            this.isTrimValue = isTrimValue;
        }

        @Override
        public U parse(String data) throws IOException {
            return Translators.translate(MapConfig.parseMap(fileName, data, isTrimValue), clazz);
        }
    }

    private static class ListBeanParser<U> implements Parser<List<U>> {

        private final Class<U> clazz;

        private final TableConfig.TableParser tableParser;

        public ListBeanParser(Class<U> clazz, boolean isTrimValue) {
            this.clazz = clazz;
            this.tableParser = TableConfig.getParser(isTrimValue);
        }

        @Override
        public List<U> parse(String data) throws IOException {
            Table<String, String, String> table = tableParser.parse(data);
            ImmutableList.Builder<U> builder = ImmutableList.builder();
            for (Map<String, String> map : table.rowMap().values()) {
                U bean = Translators.translate(map, clazz);
                builder.add(bean);
            }
            return builder.build();
        }
    }

    private static class MapBeanParser<U> implements Parser<Map<String, U>> {

        private final Class<U> clazz;

        private final TableConfig.TableParser tableParser;

        public MapBeanParser(Class<U> clazz, boolean isTrimValue) {
            this.clazz = clazz;
            this.tableParser = TableConfig.getParser(isTrimValue);
        }

        @Override
        public Map<String, U> parse(String data) throws IOException {
            Table<String, String, String> table = tableParser.parse(data);
            ImmutableMap.Builder<String, U> builder = ImmutableMap.builder();
            for (Map.Entry<String, Map<String, String>> rowInfo : table.rowMap().entrySet()) {
                String key = rowInfo.getKey();
                U bean = Translators.translate(rowInfo.getValue(), clazz);
                builder.put(key, bean);
            }

            return builder.build();
        }
    }

    /**
     * 别的项目里有使用，不能删除
     *
     * @param <T>
     */
    public static abstract class MapParser<T> implements Parser<T> {

        @Override
        public T parse(String data) throws IOException {
            return parse(MapConfig.parseMap("stub", data, false));
        }

        protected abstract T parse(Map<String, String> map);
    }

    public static final Parser<String> STRING_PARSER = new Parser<String>() {
        @Override
        public String parse(String data) throws IOException {
            return data;
        }
    };
}
