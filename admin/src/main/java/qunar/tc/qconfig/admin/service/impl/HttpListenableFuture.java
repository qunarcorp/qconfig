package qunar.tc.qconfig.admin.service.impl;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: zhaohuiyu
 * Date: 5/27/14
 * Time: 1:14 PM
 */
final class HttpListenableFuture<T> implements ListenableFuture<T> {

    private final com.ning.http.client.ListenableFuture<T> future;

    public static <U> HttpListenableFuture<U> wrap(com.ning.http.client.ListenableFuture<U> future) {
        return new HttpListenableFuture<U>(future);
    }

    private HttpListenableFuture(com.ning.http.client.ListenableFuture<T> future) {
        this.future = future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        future.addListener(listener, executor);
    }
}

