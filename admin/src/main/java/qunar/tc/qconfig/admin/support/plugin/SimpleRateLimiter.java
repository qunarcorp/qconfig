package qunar.tc.qconfig.admin.support.plugin;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QMapConfig;

import java.util.Map;

/**
 * Created by @author zhuYongMing on 2018/12/25.
 * simple rate limit tool 通用的速率限制工具，基于Google guava RateLimiter（令牌桶）实现
 */
@Component("simpleRateLimiter")
public class SimpleRateLimiter extends TrafficLimiter {

    private static final String ADMIN_RATE_LIMIT = "admin.rate.limit";

    private static volatile Map<String, RateLimiter> rateLimiterMap = Maps.newConcurrentMap();

    private static volatile String currentConfig = "";


    @QMapConfig(value = "config.properties", key = ADMIN_RATE_LIMIT)
    private void onLoad(String config) {
        // config like this : key1:速率,key2:速率（单位秒）
        if (currentConfig.equals(config)) {
            // 如果config.properties文件改动，但本key并无变化，不允许触发本速率限制变更
            return;
        }

        final Map<String, Integer> limiter = parse(config);
        Map<String, RateLimiter> newRateLimiterMap = Maps.newConcurrentMap();
        for (Map.Entry<String, Integer> entry : limiter.entrySet()) {
            newRateLimiterMap.put(entry.getKey(), RateLimiter.create(entry.getValue()));
        }

        rateLimiterMap = newRateLimiterMap;

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

        final RateLimiter rateLimiter = rateLimiterMap.get(key);
        if (rateLimiter == null) {
            // 未对其做限速处理
            return true;
        }

        return rateLimiter.tryAcquire(permits);
    }

    @Override
    public void releasePermit(String key) {
        throw new UnsupportedOperationException("RateLimit Unsupported release permits");
    }

    @Override
    public void releasePermits(String key, Integer permits) {
        throw new UnsupportedOperationException("RateLimit Unsupported release permits");
    }
}
