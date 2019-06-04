package qunar.tc.qconfig.client.validate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.common.util.QTableError;

import java.util.*;

/**
 * @author zhenyu.nie created on 2017 2017/2/15 18:07
 */
class DefaultValidateQTable implements ValidateQTable {

    private final ImmutableMap<String, Integer> rowKeyToIndex;
    private final ImmutableMap<String, Integer> columnKeyToIndex;

    private final int rowSize;
    private final int columnSize;

    private final int size;

    private final Map<String, Row> rows;
    private final Map<String, Column> columns;

    private final int[] countInRow;
    private final int[] countInColumn;

    private final Cell[][] cells;

    private final List<Cell> cellList;

    private final QTableErrorStore errorStore = new QTableErrorStoreImpl();

    public DefaultValidateQTable(QTable table) {
        rowKeyToIndex = makeIndex(table.rowKeySet());
        columnKeyToIndex = makeIndex(table.columnKeySet());
        rowSize = rowKeyToIndex.size();
        columnSize = columnKeyToIndex.size();

        Set<Table.Cell<String, String, String>> inputCells = table.cellSet();
        size = inputCells.size();
        cells = new Cell[rowSize][columnSize];
        countInRow = new int[rowSize];
        countInColumn = new int[columnSize];
        ImmutableList.Builder<Cell> cellListBuilder = ImmutableList.builder();
        for (Table.Cell<String, String, String> inputCell : inputCells) {
            int rowIndex = rowKeyToIndex.get(inputCell.getRowKey());
            int columnIndex = columnKeyToIndex.get(inputCell.getColumnKey());
            Cell cell = new Cell(inputCell.getRowKey(), inputCell.getColumnKey(), inputCell.getValue());
            cells[rowIndex][columnIndex] = cell;
            cellListBuilder.add(cell);
            countInRow[rowIndex]++;
            countInColumn[columnIndex]++;
        }
        cellList = cellListBuilder.build();

        ImmutableMap.Builder<String, Row> rowsBuilder = ImmutableMap.builder();
        for (String row : table.rowKeySet()) {
            rowsBuilder.put(row, new Row(row));
        }
        rows = rowsBuilder.build();

        ImmutableMap.Builder<String, Column> columnsBuilder = ImmutableMap.builder();
        for (String column : table.columnKeySet()) {
            columnsBuilder.put(column, new Column(column));
        }
        columns = columnsBuilder.build();
    }

    @Override
    public List<ValidateQCell> cells() {
        return convert(cellList);
    }

    @SuppressWarnings("unchecked")
    private <Father, Child extends Father> List<Father> convert(List<Child> list) {
        return (List<Father>) list;
    }

    @SuppressWarnings("unchecked")
    private <Father, Child extends Father> Collection<Father> convert(Collection<Child> c) {
        return (Collection<Father>) c;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(String row, String column) {
        Integer rowIndex = rowKeyToIndex.get(row);
        if (rowIndex == null) {
            return false;
        }
        Integer columnIndex = columnKeyToIndex.get(column);
        if (columnIndex == null) {
            return false;
        }

        return cells[rowIndex][columnIndex] != null;
    }

    @Override
    public boolean containsRow(String row) {
        return rowKeyToIndex.get(row) != null;
    }

    @Override
    public boolean containsColumn(String column) {
        return columnKeyToIndex.get(column) != null;
    }

    @Override
    public Collection<ValidateQRow> rows() {
        return convert(rows.values());
    }

    @Override
    public Row row(String rowKey) {
        Row row = rows.get(rowKey);
        return row != null ? row : new Row(rowKey);
    }

    @Override
    public Collection<ValidateQColumn> columns() {
        return convert(columns.values());
    }

    @Override
    public Column column(String columnKey) {
        Column column = columns.get(columnKey);
        return column != null ? column : new Column(columnKey);
    }

    @Override
    public Cell get(String row, String column) {
        Integer rowIndex = rowKeyToIndex.get(row);
        if (rowIndex == null) {
            return new Cell(row, column, null);
        }
        Integer columnIndex = columnKeyToIndex.get(column);
        if (columnIndex == null) {
            return new Cell(row, column, null);
        }

        Cell cell = cells[rowIndex][columnIndex];
        return cell != null ? cell : new Cell(row, column, null);
    }

    private static ImmutableMap<String, Integer> makeIndex(Set<String> set) {
        ImmutableMap.Builder<String, Integer> indexBuilder = ImmutableMap.builder();
        int i = 0;
        for (String key : set) {
            indexBuilder.put(key, i);
            i++;
        }
        return indexBuilder.build();
    }

    public QTableError getError() {
        return new QTableError(errorStore.getTableError(), errorStore.getRowError(),
                errorStore.getColumnError(), errorStore.getCellError().rowMap());
    }

    @Override
    public void error(String errorInfo) {
        if (errorInfo != null) {
            errorStore.recordTableError(errorInfo);
        }
    }

    public class Cell implements ValidateQCell {

        private final String row;

        private final String column;

        private final String value;

        public Cell(String row, String column, String value) {
            this.row = row;
            this.column = column;
            this.value = value;
        }

        public String getRow() {
            return row;
        }

        public String getColumn() {
            return column;
        }

        public boolean isPresent() {
            return value != null;
        }

        public boolean absent() {
            return value == null;
        }

        public String value() {
            return value;
        }

        public String getString() {
            return value();
        }

        public boolean getBoolean() {
            return Boolean.parseBoolean(value);
        }

        public byte getByte() {
            return Byte.parseByte(value);
        }

        public short getShort() {
            return Short.parseShort(value);
        }

        public int getInt() {
            return Integer.parseInt(value);
        }

        public long getLong() {
            return Long.parseLong(value);
        }

        public float getFloat() {
            return Float.parseFloat(value);
        }

        public double getDouble() {
            return Double.parseDouble(value);
        }

        public Date getDate() {
            return new Date(Long.parseLong(value));
        }

        @Override
        public void error(String errorInfo) {
            if (errorInfo != null) {
                errorStore.recordCellError(row, column, errorInfo);
            }
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "row='" + row + '\'' +
                    ", column='" + column + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public class Row implements ValidateQRow {

        private final String row;

        private final Integer rowIndex;

        private Row(String row) {
            this.row = row;
            rowIndex = rowKeyToIndex.get(row);
        }

        public String getRowKey() {
            return row;
        }

        public int size() {
            return rowIndex == null ? 0 : countInRow[rowIndex];
        }

        public Cell get(String column) {
            if (rowIndex == null) {
                return new Cell(row, column, null);
            }
            Integer columnIndex = columnKeyToIndex.get(column);
            if (columnIndex == null) {
                return new Cell(row, column, null);
            }

            Cell cell = cells[rowIndex][columnIndex];
            return cell != null ? cell : new Cell(row, column, null);
        }

        @Override
        public void error(String errorInfo) {
            if (errorInfo != null) {
                errorStore.recordRowError(row, errorInfo);
            }
        }

        @Override
        public String toString() {
            return "Row{" +
                    "row='" + row + '\'' +
                    '}';
        }
    }

    public class Column implements ValidateQColumn {

        private final String column;

        private final Integer columnIndex;

        private Column(String column) {
            this.column = column;
            columnIndex = rowKeyToIndex.get(column);
        }

        public String getColumnKey() {
            return column;
        }

        public int size() {
            return columnIndex == null ? 0 : countInColumn[columnIndex];
        }

        public Cell get(String row) {
            if (columnIndex == null) {
                return new Cell(row, column, null);
            }
            Integer rowIndex = rowKeyToIndex.get(row);
            if (rowIndex == null) {
                return new Cell(row, column, null);
            }

            Cell cell = cells[rowIndex][columnIndex];
            return cell != null ? cell : new Cell(row, column, null);
        }

        @Override
        public void error(String errorInfo) {
            if (errorInfo != null) {
                errorStore.recordColumnError(column, errorInfo);
            }
        }

        @Override
        public String toString() {
            return "Column{" +
                    "column='" + column + '\'' +
                    '}';
        }
    }
}
