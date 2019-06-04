package qunar.tc.qconfig.admin.service.template.detailchecker;

import com.google.common.base.Splitter;

import java.util.Collection;

public class ListDetailChecker extends AbstractCollectionChecker {

    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    public ListDetailChecker(String name) {
        super(name);
    }

    @Override
    protected Collection parseElements(String value) {
        return SPLITTER.splitToList(value);
    }

}
