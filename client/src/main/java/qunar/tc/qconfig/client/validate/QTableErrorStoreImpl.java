package qunar.tc.qconfig.client.validate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/2/17 19:58
 */
class QTableErrorStoreImpl implements QTableErrorStore, Serializable {

    private List<String> tableError = Lists.newArrayList();

    private Map<String, List<String>> rowError = Maps.newHashMap();

    private Map<String, List<String>> columnError = Maps.newHashMap();

    private Table<String, String, List<String>> cellError = HashBasedTable.create();

    @Override
    public List<String> getTableError() {
        return tableError;
    }

    @Override
    public Map<String, List<String>> getRowError() {
        return rowError;
    }

    @Override
    public Map<String, List<String>> getColumnError() {
        return columnError;
    }

    @Override
    public Table<String, String, List<String>> getCellError() {
        return cellError;
    }

    @Override
    public void recordTableError(String error) {
        tableError.add(error);
    }

    @Override
    public void recordRowError(String row, String error) {
        List<String> errors = rowError.get(row);
        if (errors == null) {
            errors = Lists.newArrayList();
            rowError.put(row, errors);
        }
        errors.add(error);
    }

    @Override
    public void recordColumnError(String column, String error) {
        List<String> errors = columnError.get(column);
        if (errors == null) {
            errors = Lists.newArrayList();
            columnError.put(column, errors);
        }
        errors.add(error);
    }

    @Override
    public void recordCellError(String row, String column, String error) {
        List<String> errors = cellError.get(row, column);
        if (errors == null) {
            errors = Lists.newArrayList();
            cellError.put(row, column, errors);
        }
        errors.add(error);
    }
}
