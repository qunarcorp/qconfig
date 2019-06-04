package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SetDetailChecker extends AbstractCollectionChecker {

    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    public SetDetailChecker(String name) {
        super(name);
    }

    @Override
    protected Collection parseElements(String value) {
        List<String> items = SPLITTER.splitToList(value);
        checkDuplicated(items);
        return items;
    }

    private void checkDuplicated(List<String> items) {
        Set<String> checkSet = Sets.newHashSetWithExpectedSize(items.size());
        for (String item : items) {
            if (!checkSet.add(item)) {
                throw new IllegalArgumentException(String.format("%s中包含重复的元素:[%s]", name(), item));
            }
        }
    }
}
