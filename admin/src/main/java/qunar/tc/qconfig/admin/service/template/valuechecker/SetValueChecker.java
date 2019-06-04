package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetValueChecker extends AbstractCollectionValueChecker<String> {

    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    public SetValueChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected Collection<String> parseElements(String value) {
        Iterable<String> items=  SPLITTER.split(value);
        Set<String> set = new HashSet<>();
        for (String item : items) {
            if (!set.add(item)) {
                throw new IllegalArgumentException(String.format("%s的值中元素[%s]重复", name(), item));
            }
        }
        return set;
    }
}
