package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;

import java.util.Collection;

public class ListValueChecker extends AbstractCollectionValueChecker<String> {

    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    public ListValueChecker(ObjectNode node) {
        super(node);
    }

    @Override
    protected Collection parseElements(String value) {
        return SPLITTER.splitToList(value);
    }
}
