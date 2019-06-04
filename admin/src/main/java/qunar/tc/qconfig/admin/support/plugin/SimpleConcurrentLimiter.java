package qunar.tc.qconfig.admin.support.plugin;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QMapConfig;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by @author zhuYongMing on 2018/12/4.
 * simple concurrent limit tool 并发限制工具
 */
@Component("simpleConcurrentLimiter")
public class SimpleConcurrentLimiter extends TrafficLimiter {

    private static final String ADMIN_CONCURRENT_LIMIT = "admin.concurrent.limit";

    private static volatile Map<String, Semaphore> semaphoreMap = Maps.newConcurrentMap();

    private static volatile String currentConfig = "";


    @QMapConfig(value = "config.properties", key = ADMIN_CONCURRENT_LIMIT)
    private void onLoad(String config) {
        // config value like this : titan:100,xxx1:50,xxx2:300   key1:最大并发数1,key2:最大并发数2
        if (currentConfig.equals(config)) {
            // 如果config.properties文件改动，但本key并无变化，不允许触发本并发限制变更
            return;
        }

        Map<String, Integer> limiter = parse(config);
        Map<String, Semaphore> newSemaphoreMap = Maps.newConcurrentMap();
        for (Map.Entry<String, Integer> entry : limiter.entrySet()) {
            newSemaphoreMap.put(entry.getKey(), new Semaphore(entry.getValue()));
        }
        semaphoreMap = newSemaphoreMap;

        currentConfig = config;
    }

    @Override
    public boolean takePermit(String key) {
        return takePermits(key, 1);
    }

    @Override
    public boolean takePermits(String key, Integer permits) {
        if (permits == null || permits <= 0) {
            return true;
        }

        final Semaphore semaphore = semaphoreMap.get(key);
        if (semaphore == null) {
            // 未对其做限制并发控制
            return true;
        }

        return semaphore.tryAcquire(permits);
    }

    @Override
    public void releasePermit(String key) {
        releasePermits(key, 1);
    }

    @Override
    public void releasePermits(String key, Integer permits) {
        if (permits == null || permits <= 0) {
            return;
        }

        final Semaphore semaphore = semaphoreMap.get(key);
        if (semaphore != null) {
            semaphore.release(permits);
        }
    }
}
