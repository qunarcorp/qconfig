package qunar.tc.qconfig.client;

import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * @author zhenyu.nie created on 2017 2017/4/14 17:50
 */
public class PropertyItem {

    public enum Type {
        Add, Delete, Modify, NoChange
    }

    private final Type type;

    private final String key;

    private final String oldValue;

    private final String newValue;

    public PropertyItem(Type type, String key, String oldValue, String newValue) {
        this.type = type;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Optional<String> getOldString() {
        return Optional.fromNullable(oldValue);
    }

    public Optional<String> getNewString() {
        return Optional.fromNullable(newValue);
    }

    public String getOldString(String def) {
        return oldValue != null ? oldValue : def;
    }

    public String getNewString(String def) {
        return newValue != null ? newValue : def;
    }

    public Optional<Boolean> getOldBoolean() {
        return parseOld(booleanParser);
    }

    public Optional<Boolean> getNewBoolean() {
        return parseNew(booleanParser);
    }

    public boolean getOldBoolean(boolean def) {
        return parseOld(booleanParser, def);
    }

    public boolean getNewBoolean(boolean def) {
        return parseNew(booleanParser, def);
    }

    public Optional<Integer> getOldInt() {
        return parseOld(intParser);
    }

    public Optional<Integer> getNewInt() {
        return parseNew(intParser);
    }

    public int getOldInt(int def) {
        return parseOld(intParser, def);
    }

    public int getNewInt(int def) {
        return parseNew(intParser, def);
    }

    public Optional<Long> getOldLong() {
        return parseOld(longParser);
    }

    public Optional<Long> getNewLong() {
        return parseNew(longParser);
    }

    public long getOldLong(long def) {
        return parseOld(longParser, def);
    }

    public long getNewLong(long def) {
        return parseNew(longParser, def);
    }

    public Optional<Float> getOldFloat() {
        return parseOld(floatParser);
    }

    public Optional<Float> getNewFloat() {
        return parseNew(floatParser);
    }

    public float getOldFloat(float def) {
        return parseOld(floatParser, def);
    }

    public float getNewFloat(float def) {
        return parseNew(floatParser, def);
    }

    public Optional<Double> getOldDouble() {
        return parseOld(doubleParser);
    }

    public Optional<Double> getNewDouble() {
        return parseNew(doubleParser);
    }

    public double getOldDouble(double def) {
        return parseOld(doubleParser, def);
    }

    public double getNewDouble(double def) {
        return parseNew(doubleParser, def);
    }

    private <T> Optional<T> parseOld(Function<String, T> parser) {
        return parse(oldValue, parser);
    }

    private <T> Optional<T> parseNew(Function<String, T> parser) {
        return parse(newValue, parser);
    }

    private <T> T parseOld(Function<String, T> parser, T def) {
        return parse(oldValue, parser, def);
    }

    private <T> T parseNew(Function<String, T> parser, T def) {
        return parse(newValue, parser, def);
    }

    private <T> Optional<T> parse(String value, Function<String, T> parser) {
        if (value == null) {
            return Optional.absent();
        }

        try {
            T result = parser.apply(value);
            return Optional.fromNullable(result);
        } catch (NumberFormatException e) {
            return Optional.absent();
        }
    }

    private <T> T parse(String value, Function<String, T> parser, T def) {
        Optional<T> result = parse(value, parser);
        return result.isPresent() ? result.get() : def;
    }

    private static final Function<String, Boolean> booleanParser = new Function<String, Boolean>() {
        @Override
        public Boolean apply(String input) {
            return Boolean.valueOf(input);
        }
    };

    private static final Function<String, Integer> intParser = new Function<String, Integer>() {
        @Override
        public Integer apply(String input) {
            return Integer.valueOf(input);
        }
    };

    private static final Function<String, Long> longParser = new Function<String, Long>() {
        @Override
        public Long apply(String input) {
            return Long.valueOf(input);
        }
    };

    private static final Function<String, Float> floatParser = new Function<String, Float>() {
        @Override
        public Float apply(String input) {
            return Float.valueOf(input);
        }
    };

    private static final Function<String, Double> doubleParser = new Function<String, Double>() {
        @Override
        public Double apply(String input) {
            return Double.valueOf(input);
        }
    };

    @Override
    public String toString() {
        return "PropertyItem{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
