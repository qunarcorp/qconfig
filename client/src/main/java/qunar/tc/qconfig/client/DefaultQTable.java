package qunar.tc.qconfig.client;

import com.google.common.collect.Table;

/**
 * @author zhenyu.nie created on 2016 2016/4/8 11:05
 */
public class DefaultQTable extends ForwardingQTable implements QTable {

    private Table<String, String, String> table;

    public DefaultQTable(Table<String, String, String> table) {
        this.table = table;
    }

    @Override
    protected Table<String, String, String> delegate() {
        return table;
    }
}
