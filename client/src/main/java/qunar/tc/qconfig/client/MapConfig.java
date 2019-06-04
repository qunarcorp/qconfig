package qunar.tc.qconfig.client;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import qunar.tc.qconfig.client.impl.AbstractConfiguration;
import qunar.tc.qconfig.client.impl.ConfigEngine;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-12.
 */
public class MapConfig extends AbstractConfiguration<Map<String, String>> {

    private static final Logger logger = LoggerFactory.getLogger(MapConfig.class);

    private static final DataLoader loader = ConfigEngine.getInstance();

    private static final MapConfigParameterReplaceTool replaceTool = MapConfigParameterReplaceTool.getInstance();

    private static final DataLoader.Generator gen = new DataLoader.Generator<Map<String, String>>() {
        @Override
        public AbstractConfiguration<Map<String, String>> create(Feature feature, String fileName) {
            return new MapConfig(feature, fileName);
        }
    };

    // 文件内容有改变的时候才会触发
    public interface PropertiesChangeListener {
        void onChange(PropertiesChange change);
    }

    private final PropertiesChangedManager propertiesChangedManager = new PropertiesChangedManager();

    // 因为之前是public的，所以保留
    public MapConfig(Feature feature) {
        this(feature, STUB_FILE_NAME);
    }

    private MapConfig(Feature feature, String fileName) {
        super(feature, fileName);
        addListener(propertiesChangedManager);
    }

    public static MapConfig get(String groupName, String fileName, Feature feature) {
        return (MapConfig) loader.load(groupName, fileName, feature, gen);
    }

    public static MapConfig get(String fileName, Feature feature) {
        return get(null, fileName, feature);
    }

    public static MapConfig get(String fileName) {
        return get(null, fileName, Feature.DEFAULT);
    }

    public void addPropertiesListener(PropertiesChangeListener listener) {
        propertiesChangedManager.addPropertiesListener(listener);
    }

    // 因为之前是public的，所以保留
    public static Map<String, String> parseMap(String data, boolean trimValue) throws IOException {
        return parseMap(STUB_FILE_NAME, data, trimValue);
    }

    public static Map<String, String> parseMap(String fileName, String data, boolean trimValue) throws IOException {
        if (data == null) {
            return ImmutableMap.of();
        }

        if (FileChecker.isTemplateFile(fileName)) {
            return parseTable(data, trimValue);
        }

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (!Character.isSpaceChar(c)) {
                if (c == '<') {
                    return parseXML(data);
                } else {
                    return parseProperties(data, trimValue);
                }
            }
        }
        return ImmutableMap.of();
    }

    private static Map<String, String> parseTable(String data, boolean trimValue) throws IOException {
        if (data == null) {
            return ImmutableMap.of();
        }

        TableConfig.TableParser parser;
        if (trimValue) {
            parser = TableConfig.TRIM_PARSER;
        } else {
            parser = TableConfig.NOT_TRIM_PARSER;
        }

        Table<String, String, String> table = parser.parse(data);
        Map<String, String> map = new LinkedHashMap<String, String>(table.size());
        for (Table.Cell<String, String, String> cell : table.cellSet()) {
            map.put(generateKey(cell.getRowKey(), cell.getColumnKey()), cell.getValue());
        }
        return ImmutableMap.copyOf(map);
    }

    private static String generateKey(String row, String column) {
        if (Strings.isNullOrEmpty(row)) {
            return column;
        }

        if (Strings.isNullOrEmpty(column)) {
            return row;
        }

        return row + Constants.ROW_COLUMN_SEPARATOR + column;
    }

    public static Map<String, String> parseProperties(String data, boolean trimValue) throws IOException {
        if (data == null)
            return ImmutableMap.of();

        Properties p = new Properties();

        p.load(new StringReader(data));

        Map<String, String> map = new LinkedHashMap<String, String>(p.size());

        for (String key : p.stringPropertyNames())
            map.put(key, getValue(p.getProperty(key), trimValue));
        return ImmutableMap.copyOf(map);
    }

    private static String getValue(String value, boolean trimValue) {
        if (trimValue && value != null) {
            return value.trim();
        }
        return value;
    }


    public static Map<String, String> parseXML(String data) throws IOException {
        if (data == null) return ImmutableMap.of();

        try {
            Reader reader = new StringReader(data);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(reader));
            XPathFactory factory = XPathFactory.newInstance();
            NodeList nodes = (NodeList) factory.newXPath().compile("/properties/property")
                    .evaluate(doc, XPathConstants.NODESET);

            Map<String, String> map = new LinkedHashMap<String, String>();

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String name = el.getAttribute("name");
                String value = getString(el.getAttribute("value"), el.getTextContent());
                map.put(name, value);
            }

            return ImmutableMap.copyOf(map);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    //用户拿到这个引用，每次配置变更，这个引用里面的数据也会变
    private final DynamicMap ref = new DynamicMap(current);

    @Override
    public Map<String, String> emptyData() {
        return ImmutableMap.of();
    }

    @Override
    public Map<String, String> parse(String data) throws IOException {
        Map<String, String> result = parseMap(fileName, data, feature == null || feature.isTrimValue());
        return replaceTool.replace(fileName, result, feature == null || feature.isTrimValue());
    }

    /**
     * 每次配置变更，该map里的数据就会变化，持有该map引用每次取会取到最新值
     *
     * @return
     */
    public Map<String, String> asMap() {
        waitFistLoad();
        return ref;
    }

    public Properties asProperties() {
        Properties prop = new Properties();
        prop.putAll(asMap());
        return prop;
    }

    private class DynamicMap implements Map<String, String> {

        private final AtomicReference<Map<String, String>> current;

        public DynamicMap(AtomicReference<Map<String, String>> current) {
            this.current = current;
        }

        private Map<String, String> delegate() {
            return current.get();
        }

        @Override
        public int size() {
            return delegate().size();
        }

        @Override
        public boolean isEmpty() {
            return delegate().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate().containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate().containsValue(value);
        }

        @Override
        public String get(Object key) {
            return delegate().get(key);
        }

        @Override
        public String put(String key, String value) {
            logger.error("qconfig map is immutable!");
            throw new UnsupportedOperationException();
        }

        @Override
        public String remove(Object key) {
            logger.error("qconfig map is immutable!");
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            logger.error("qconfig map is immutable!");
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            logger.error("qconfig map is immutable!");
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            return delegate().keySet();
        }

        @Override
        public Collection<String> values() {
            return delegate().values();
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return delegate().entrySet();
        }

        @Override
        public String toString() {
            Map<String, String> map = delegate();
            if (map == null) return "{}";
            return map.toString();
        }
    }

    private static String getString(Object o, Object... args) {

        String str = o == null ? null : o.toString();

        if (args == null || args.length == 0 || (str != null && !str.isEmpty()))
            return str;

        for (int i = 0; i < args.length; i++) {
            str = args[i] == null ? null : args[i].toString();
            if (str != null && !str.isEmpty())
                return str;
        }

        return str;
    }
}
