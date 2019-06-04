package qunar.tc.qconfig.client.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.Constants;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by zhaohui.yu
 * 1/28/18
 */
class QueueQConfigServerClient implements QConfigServerClient {
    private static final Logger LOG = LoggerFactory.getLogger(QueueQConfigServerClient.class);

    private static final int TYPE_LOAD_DATA = 0;
    private static final int TYPE_FORCE_LOAD = 1;

    private static final int CONCURRENCY_LIMIT = 20;

    private final QConfigServerClient impl;
    private final BlockingDeque<Request> requests = new LinkedBlockingDeque<Request>();
    private int concurrentRequestCount = 0;

    QueueQConfigServerClient(QConfigServerClient impl) {
        this.impl = impl;
    }

    @Override
    public ListenableFuture<TypedCheckResult> checkUpdate(Map<Meta, VersionProfile> files) {
        return impl.checkUpdate(files);
    }

    @Override
    public ListenableFuture<TypedCheckResult> loadGroupFiles() {
        return impl.loadGroupFiles();
    }

    @Override
    public ListenableFuture<TypedCheckResult> longPollingCheckUpdate(Map<Meta, VersionProfile> files) {
        return impl.longPollingCheckUpdate(files);
    }

    @Override
    public ListenableFuture<Snapshot<String>> loadData(Meta key, VersionProfile version, Feature feature) {
        final Request request = new Request(TYPE_LOAD_DATA, key, version, -1, feature);
        return submitRequest(request, false);
    }

    @Override
    public ListenableFuture<Snapshot<String>> forceReload(Meta key, long minVersion, Feature feature) {
        final Request request = new Request(TYPE_FORCE_LOAD, key, null, minVersion, feature);
        return submitRequest(request, true);
    }

    private ListenableFuture<Snapshot<String>> submitRequest(Request request, boolean first) {
        final SettableFuture<Snapshot<String>> result = SettableFuture.create();
        request.result = result;

        final boolean executeNow;
        synchronized (this) {
            if (concurrentRequestCount >= CONCURRENCY_LIMIT) {
                executeNow = false;
                if (first) {
                    requests.offerFirst(request);
                } else {
                    requests.offerLast(request);
                }
            } else {
                executeNow = true;
                concurrentRequestCount++;
            }
        }

        if (executeNow) {
            process(request);
        }
        return result;
    }

    private void process(final Request request) {
        final ListenableFuture<Snapshot<String>> future = executeRequest(request);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                triggerNextQueuedRequests();

                try {
                    final Snapshot<String> snapshot = future.get();
                    request.result.set(snapshot);
                } catch (ExecutionException e) {
                    request.result.setException(e.getCause());
                } catch (Throwable e) {
                    request.result.setException(e);
                }
            }
        }, Constants.CURRENT_EXECUTOR);
    }

    private ListenableFuture<Snapshot<String>> executeRequest(final Request request) {
        if (request.type == TYPE_LOAD_DATA) {
            return impl.loadData(request.key, request.version, request.feature);
        } else {
            return impl.forceReload(request.key, request.minVersion, request.feature);
        }
    }

    private void triggerNextQueuedRequests() {
        try {
            final Request request;
            synchronized (this) {
                request = requests.pollFirst();
                if (request == null) {
                    concurrentRequestCount--;
                    return;
                }
            }
            process(request);
        } catch (Throwable e) {
            LOG.error("process qconfig request failed", e);
        }
    }

    @Override
    public void recordLoading(ConfigLogType type, Meta meta, long version, String errorInfo) throws IOException {
        impl.recordLoading(type, meta, version, errorInfo);
    }

    private static class Request {
        public final int type;

        public final Meta key;

        public final VersionProfile version;

        public final long minVersion;

        public final Feature feature;

        public SettableFuture<Snapshot<String>> result;

        private Request(int type, Meta key, VersionProfile version, long minVersion, Feature feature) {
            this.type = type;
            this.key = key;
            this.version = version;
            this.minVersion = minVersion;
            this.feature = feature;
        }
    }
}
