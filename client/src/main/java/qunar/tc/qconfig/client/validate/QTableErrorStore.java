package qunar.tc.qconfig.client.validate;

import com.google.common.collect.Table;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/2/17 17:00
 */
interface QTableErrorStore {

    List<String> getTableError();

    Map<String, List<String>> getRowError();

    Map<String, List<String>> getColumnError();

    Table<String, String, List<String>> getCellError();

    void recordTableError(String error);

    void recordRowError(String row, String error);

    void recordColumnError(String column, String error);

    void recordCellError(String row, String column, String error);
}
