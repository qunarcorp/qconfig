package qunar.tc.qconfig.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/4/14 17:52
 */
public class PropertiesChange {

    private final boolean change;

    private final Map<String, PropertyItem> items;

    PropertiesChange(Map<String, String> add, Map<String, String> delete, Map<String, MapDifference.ValueDifference<String>> modify, Map<String, String> invariant) {
        change = !add.isEmpty() || !delete.isEmpty() || !modify.isEmpty();

        ImmutableMap.Builder<String, PropertyItem> builder = new ImmutableMap.Builder<String, PropertyItem>();
        for (Map.Entry<String, String> entry : add.entrySet()) {
            builder.put(entry.getKey(), new PropertyItem(PropertyItem.Type.Add, entry.getKey(), null, entry.getValue()));
        }
        for (Map.Entry<String, String> entry : delete.entrySet()) {
            builder.put(entry.getKey(), new PropertyItem(PropertyItem.Type.Delete, entry.getKey(), entry.getValue(), null));
        }
        for (Map.Entry<String, MapDifference.ValueDifference<String>> entry : modify.entrySet()) {
            builder.put(entry.getKey(), new PropertyItem(PropertyItem.Type.Modify, entry.getKey(), entry.getValue().leftValue(), entry.getValue().rightValue()));
        }
        for (Map.Entry<String, String> entry : invariant.entrySet()) {
            builder.put(entry.getKey(), new PropertyItem(PropertyItem.Type.NoChange, entry.getKey(), entry.getValue(), entry.getValue()));
        }
        items = builder.build();
    }

    boolean hasChange() {
        return change;
    }

    public boolean isChange(String key) {
        PropertyItem item = items.get(key);
        return item != null && item.getType() != PropertyItem.Type.NoChange;
    }

    public Map<String, PropertyItem> getItems() {
        return items;
    }

    public static PropertiesChange diff(Map<String, String> oldConfig, Map<String, String> newConfig) {
        MapDifference<String, String> diff = Maps.difference(oldConfig, newConfig);
        Map<String, String> add = diff.entriesOnlyOnRight();
        Map<String, String> delete = diff.entriesOnlyOnLeft();
        Map<String, MapDifference.ValueDifference<String>> modify = diff.entriesDiffering();
        Map<String, String> noChange = diff.entriesInCommon();
        return new PropertiesChange(add, delete, modify, noChange);
    }

    @Override
    public String toString() {
        return "PropertiesChange{" +
                "items=" + items +
                '}';
    }
}
