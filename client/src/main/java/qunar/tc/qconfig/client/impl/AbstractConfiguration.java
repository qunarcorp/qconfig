package qunar.tc.qconfig.client.impl;


import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.exception.ResultUnexpectedException;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-9.
 */
public abstract class AbstractConfiguration<T> implements Configuration<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractConfiguration.class);

    protected final Feature feature;

    private final InitFuture future = new InitFuture();

    private final CopyOnWriteArraySet<ConfigListener<T>> listeners = new CopyOnWriteArraySet<ConfigListener<T>>();

    protected final AtomicReference<T> current = new AtomicReference<T>();

    protected final String fileName;

    protected static final String STUB_FILE_NAME = "stub";

    private static final int MAX_ALLOWED_LISTENERS = 50;

    private final AtomicInteger listenerCount = new AtomicInteger();

    public AbstractConfiguration(Feature feature) {
        this(feature, STUB_FILE_NAME);
    }

    public AbstractConfiguration(Feature feature, String fileName) {
        this.feature = feature;
        this.fileName = fileName;
    }

    @Override
    public ListenableFuture<Boolean> initFuture() {
        return future;
    }

    protected void waitFistLoad() {
        if (current.get() != null) return;
        try {
            initFuture().get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ResultUnexpectedException) {
                throw (ResultUnexpectedException) cause;
            } else {
                throw new ResultUnexpectedException(e.getMessage());
            }
        } catch (Throwable e) {
            throw new ResultUnexpectedException(e.getMessage());
        }
    }

    boolean setData(T data, boolean trigger) {
        synchronized (current) {

            current.set(data);
            onChanged();

            if (!future.isDone()) {
                future.set(true);
            }

            return triggers(data, trigger);
        }
    }

    private boolean triggers(T data, boolean trigger) {
        if (!trigger) return true;
        boolean result = true;
        for (ConfigListener<T> listener : listeners) {
            if (!trigger(listener, data)) result = false;
        }
        return result;
    }

    boolean setData(T data) {
        return setData(data, true);
    }

    protected void onChanged() {

    }

    void setException(Exception ex) {
        if (!future.isDone()) {
            future.setException(ex);
        }
    }

    private boolean trigger(ConfigListener<T> listener, T data) {
        try {
            listener.onLoad(data);
            return true;
        } catch (Throwable e) {
            log.error("配置文件变更, 事件触发异常. data: {}", data, e);
            return false;
        }
    }

    @Override
    public void addListener(ConfigListener<T> listener) {
        if (listenerCount.incrementAndGet() > MAX_ALLOWED_LISTENERS) {
            log.error("配置文件{}的listener超过了{}个，为防止泄露直接丢弃", fileName, MAX_ALLOWED_LISTENERS);
            return;
        }
        synchronized (current) {

            if (isInit()) {
                trigger(listener, current.get());
            }
            listeners.add(listener);
        }
    }

    protected boolean isInit() {
        return future.isDone() && future.isSuccess();
    }

    private static class InitFuture extends AbstractFuture<Boolean> {
        private Throwable throwable;

        public void set(boolean value) {
            super.set(value);
        }

        public boolean setException(Throwable throwable) {
            this.throwable = throwable;
            return super.setException(throwable);
        }

        public boolean isSuccess() {
            return throwable == null;
        }
    }

    public interface Parser<T> {
        T parse(String data) throws IOException;
    }
}
