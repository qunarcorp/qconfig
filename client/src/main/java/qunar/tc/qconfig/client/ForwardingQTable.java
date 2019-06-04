package qunar.tc.qconfig.client;

import com.google.common.base.Strings;
import com.google.common.collect.ForwardingTable;

import java.util.Date;

/**
 * @author zhenyu.nie created on 2016 2016/4/8 11:16
 */
public abstract class ForwardingQTable extends ForwardingTable<String, String, String> implements QTable {

    protected ForwardingQTable() {}

    @Override
    public String getString(String row, String column) {
        return delegate().get(row, column);
    }

    @Override
    public boolean getBoolean(String row, String column) {
        return Boolean.parseBoolean(delegate().get(row, column));
    }

    @Override
    public boolean getBoolean(String row, String column, boolean defaultValue) {
        String value = getString(row, column);
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    @Override
    public byte getByte(String row, String column) {
        return Byte.parseByte(delegate().get(row, column));
    }

    @Override
    public byte getByte(String row, String column, byte defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public short getShort(String row, String column) {
        return Short.parseShort(delegate().get(row, column));
    }

    @Override
    public short getShort(String row, String column, short defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Short.parseShort(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public int getInt(String row, String column) {
        return Integer.parseInt(delegate().get(row, column));
    }

    @Override
    public int getInt(String row, String column, int defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String row, String column) {
        return Long.parseLong(delegate().get(row, column));
    }

    @Override
    public long getLong(String row, String column, long defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(String row, String column) {
        return Float.parseFloat(delegate().get(row, column));
    }

    @Override
    public float getFloat(String row, String column, float defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String row, String column) {
        return Double.parseDouble(delegate().get(row, column));
    }

    @Override
    public double getDouble(String row, String column, double defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Date getDate(String row, String column) {
        String str = delegate().get(row, column);
        return !Strings.isNullOrEmpty(str) ? new Date(Long.parseLong(str)) : null;
    }

    @Override
    public Date getDate(String row, String column, Date defaultValue) {
        String str = delegate().get(row, column);
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return new Date(Long.parseLong(str));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
