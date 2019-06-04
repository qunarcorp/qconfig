package qunar.tc.qconfig.admin.service.template.detailchecker;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import qunar.tc.qconfig.common.util.Strings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapDetailChecker extends AbstractCollectionChecker<Map.Entry<String, String>> {

    private final static Splitter SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();

    public MapDetailChecker(String name) {
        super(name);
    }

    @Override
    protected Collection<Map.Entry<String, String>> parseElements(String value) {
        Iterable<String> entries = SPLITTER.split(value);
        Map<String, String> map = new HashMap<>();
        for (String kvPair : entries) {
            int index = kvPair.indexOf(':');
            Preconditions.checkArgument(index >= 0, String.format("%s的默认值缺少\":\"分隔符", name()));
            String key = kvPair.substring(0, index);
            String val = kvPair.substring(index + 1);
            Preconditions.checkArgument(!Strings.isBlank(key), String.format("%s的默认值中key不能为空", name()));
            if (map.put(key, val) != null) {
                throw new IllegalArgumentException(String.format("%s的默认值中key[%s]重复", name(), key));
            }
        }
        return map.entrySet();
    }

}
