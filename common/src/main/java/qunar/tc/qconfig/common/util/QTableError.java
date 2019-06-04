package qunar.tc.qconfig.common.util;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 12:21
 */
public class QTableError {

    private List<String> tableError;

    private Map<String, List<String>> rowError;

    private Map<String, List<String>> columnError;

    private Map<String, Map<String, List<String>>> cellError;

    public QTableError() {
    }

    public QTableError(List<String> tableError,
                       Map<String, List<String>> rowError,
                       Map<String, List<String>> columnError, Map<String, Map<String, List<String>>> cellError) {
        this.tableError = tableError;
        this.rowError = rowError;
        this.columnError = columnError;
        this.cellError = cellError;
    }

    public boolean isError() {
        return !tableError.isEmpty() || !rowError.isEmpty() || !columnError.isEmpty() || !cellError.isEmpty();
    }

    public List<String> getTableError() {
        return tableError;
    }

    public Map<String, List<String>> getRowError() {
        return rowError;
    }

    public Map<String, List<String>> getColumnError() {
        return columnError;
    }

    public Map<String, Map<String, List<String>>> getCellError() {
        return cellError;
    }

    @Override
    public String toString() {
        return "QTableError{" +
                "tableError=" + tableError +
                ", rowError=" + rowError +
                ", columnError=" + columnError +
                ", cellError=" + cellError +
                '}';
    }
}
