package qunar.tc.qconfig.admin.support;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.Constants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Date: 14-6-10 Time: 下午7:57
 *
 * @author: xiao.liang
 * @description:
 */
public class HostnameUtil {

    private static Logger log = LoggerFactory.getLogger(HostnameUtil.class);

    private static Executor executor = Executors.newFixedThreadPool(5);

    private static final LoadingCache<String, String> ipCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.DAYS).maximumSize(30000).build(new CacheLoader<String, String>() {
                @Override
                public String load(final String ip) throws Exception {
                    final ListenableFutureTask<String> task = ListenableFutureTask.create(new Callable<String>() {
                        public String call() {
                            try {
                                return InetAddress.getByName(ip).getHostName();
                            } catch (UnknownHostException e) {
                                log.warn("unknown host name, ip={}", ip);
                                return ip;
                            }
                        }
                    });

                    executor.execute(task);

                    try {
                        return dealHostname(task.get(500, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    } catch (TimeoutException e) {
                        task.addListener(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    setHostName(ip, task.get());
                                } catch (Exception e) {
                                    // ignore
                                }
                            }
                        }, Constants.CURRENT_EXECUTOR);
                    } catch (ExecutionException e) {
                        log.error("get hostname error, ip={}", ip, e);
                    }
                    return ip;
                }
            });

    public static String getHostnameFromIp(final String ip) {
        return ipCache.getUnchecked(ip);
    }

    public static Optional<String> getIpFromHostname(final String hostname) {
        try {
            return Optional.of(InetAddress.getByName(hostname).getHostAddress());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    public static void setHostName(String ip, String hostname) {
        ipCache.put(ip, dealHostname(hostname));
    }

    private static String dealHostname(String hostname) {
        if (hostname.endsWith(QUNAR_SUFFIX)) {
            return hostname.substring(0, hostname.length() - QUNAR_SUFFIX.length());
        }
        return hostname;
    }

    private static final String QUNAR_SUFFIX = ".qunar.com";
}
