package qunar.tc.qconfig.client.impl;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientHolder {
    protected static final Logger logger = LoggerFactory.getLogger("qunar.tc.qconfig.client.impl.HttpClient");

    public static final AsyncHttpClient INSTANCE = Factory.createHttpClient();

    private static class Factory {
        private static final int CONN_TIMEOUT = 1000;
        private static final int REQUEST_TIMEOUT = 2000;
        private static final int BOSS_COUNT = 1;
        private static final int WORKER_COUNT = 2;
        private static final int LONG_POLLING_TIMEOUT = 90000;

        private static AsyncHttpClient createHttpClient() {
            HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("qconfig-timer"), new ThreadNameDeterminer() {
                @Override
                public String determineThreadName(String currentThreadName, String proposedThreadName) throws Exception {
                    return "qconfig-timer #1";
                }
            }, 100, TimeUnit.MILLISECONDS, 512);
            timer.start();
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setConnectTimeout(CONN_TIMEOUT);
            builder.setRequestTimeout(REQUEST_TIMEOUT);
            builder.setReadTimeout(LONG_POLLING_TIMEOUT);
            builder.setAllowPoolingConnections(true);
            builder.setCompressionEnforced(true);
            builder.setPooledConnectionIdleTimeout(3 * 60 * 1000);
            builder.setAcceptAnyCertificate(true);

            ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "qconfig-loader-callback");
                    t.setDaemon(true);
                    return t;
                }
            });
            builder.setExecutorService(threadPool);

            NettyAsyncHttpProviderConfig providerConfig = new NettyAsyncHttpProviderConfig();
            providerConfig.setNettyTimer(timer);
            NioClientBossPool bossPool = new NioClientBossPool(threadPool, BOSS_COUNT, timer, new ThreadNameDeterminer() {
                @Override
                public String determineThreadName(String currentThreadName, String proposedThreadName) throws Exception {
                    return "qconfig-loader boss #1";
                }
            });
            NioWorkerPool workerPool = new NioWorkerPool(threadPool, WORKER_COUNT, new ThreadNameDeterminer() {
                private final AtomicInteger i = new AtomicInteger(0);

                @Override
                public String determineThreadName(String currentThreadName, String proposedThreadName) throws Exception {
                    return "qconfig-loader worker #" + i.incrementAndGet();
                }
            });
            providerConfig.setSocketChannelFactory(new NioClientSocketChannelFactory(bossPool, workerPool));
            builder.setAsyncHttpClientProviderConfig(providerConfig);
            return new AsyncHttpClient(builder.build());
        }
    }


}
