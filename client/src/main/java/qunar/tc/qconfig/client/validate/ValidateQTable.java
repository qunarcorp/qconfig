package qunar.tc.qconfig.client.validate;

import java.util.Collection;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/2/17 19:54
 */
public interface ValidateQTable extends ErrorRecorder {

    List<ValidateQCell> cells();

    int size();

    boolean isEmpty();

    boolean contains(String row, String column);

    boolean containsRow(String row);

    boolean containsColumn(String column);

    Collection<ValidateQRow> rows();

    ValidateQRow row(String rowKey);

    Collection<ValidateQColumn> columns();

    ValidateQColumn column(String columnKey);

    ValidateQCell get(String row, String column);
}
