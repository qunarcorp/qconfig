package qunar.tc.qconfig.client;

import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhenyu.nie created on 2016 2016/4/8 11:27
 */
class DynamicQTable extends ForwardingQTable implements QTable {

    private static final Logger logger = LoggerFactory.getLogger(DynamicQTable.class);

    private final AtomicReference<? extends Table<String, String, String>> reference;

    <T extends Table<String, String, String>> DynamicQTable(AtomicReference<T> reference) {
        this.reference = reference;
    }

    @Override
    protected Table<String, String, String> delegate() {
        return reference.get();
    }

    @Override
    public void clear() {
        logger.error("qconfig table is immutable!");
        throw new UnsupportedOperationException();
    }

    @Override
    public String put(String rowKey, String columnKey, String value) {
        logger.error("qconfig table is immutable!");
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Table<? extends String, ? extends String, ? extends String> table) {
        logger.error("qconfig table is immutable!");
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object rowKey, Object columnKey) {
        logger.error("qconfig table is immutable!");
        throw new UnsupportedOperationException();
    }
}
