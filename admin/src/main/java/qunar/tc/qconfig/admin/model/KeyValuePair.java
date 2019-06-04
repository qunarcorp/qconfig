package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2017 2017/9/5 12:32
 */
public class KeyValuePair<K, V> {

    private final K key;

    private final V value;

    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KeyValuePair{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
