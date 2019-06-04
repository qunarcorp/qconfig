package qunar.tc.qconfig.server.config.longpolling.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;
import qunar.tc.qconfig.server.support.monitor.Monitor;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2018 2018/4/17 16:02
 */
public class RateLimitServiceImpl implements RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitServiceImpl.class);

    private static final int MAX_INTERVAL_SECOND = 5;

    private static final int DEFAULT_INTERVAL_SECOND = 3;

    private static final int DEFAULT_LIMIT_COUNT = 8;

    private volatile LimitInfo limitInfo = new LimitInfo(DEFAULT_INTERVAL_SECOND, DEFAULT_LIMIT_COUNT);

    private LoadingCache<IpAndPort, Limiter> cache;

    @PostConstruct
    public void init() {
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> config) {
                int newIntervalSecond = Numbers.toInt(config.get("client.check.rate.limit.interval.Second"));
                int newLimitCount = Numbers.toInt(config.get("client.check.rate.limit.count"));
                LimitInfo newLimitInfo = new LimitInfo(newIntervalSecond, newLimitCount);
                if (isLegalLimitInfo(newLimitInfo)) {
                    limitInfo = newLimitInfo;
                    logger.info("fresh client check rate limit, {}", newLimitInfo);
                } else {
                    logger.warn("illegal limit info, {}", newLimitInfo);
                }
            }
        });


        logger.info("init client check rate limit, {}", limitInfo);
        Preconditions.checkArgument(isLegalLimitInfo(limitInfo), "illegal limit info, %s", limitInfo);

        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(MAX_INTERVAL_SECOND + 1, TimeUnit.SECONDS)
                .build(new CacheLoader<IpAndPort, Limiter>() {
                    @Override
                    public Limiter load(IpAndPort address) throws Exception {
                        return new Limiter(limitInfo);
                    }
                });

        Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("clear-rate-limit")).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                cache.cleanUp();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryAcquire(IpAndPort address) {
        Limiter limiter = cache.getUnchecked(address);
        boolean acquire = limiter.tryAcquire(second());
        if (!acquire) {
            cache.invalidate(address);
            Monitor.clientCheckRateUpToLimitCounter.inc();
            logger.warn("client check rate up to limit, {}", address);
        }
        return acquire;
    }

    private int second() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private boolean isLegalLimitInfo(LimitInfo limitInfo) {
        return limitInfo.getIntervalSecond() > 0 && limitInfo.getIntervalSecond() <= MAX_INTERVAL_SECOND
                && limitInfo.getLimitCount() > 0;
    }
}
