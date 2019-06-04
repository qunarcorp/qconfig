package qunar.tc.qconfig.admin.support.plugin;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;
import qunar.tc.qconfig.common.util.Constants;

import java.util.List;
import java.util.Map;

public abstract class TrafficLimiter {

    /**
     * it does not block waiting for permits to become available ,returns immediately
     */
    public abstract boolean takePermit(String key);

    /**
     * it does not block waiting for permits to become available ,returns immediately
     */
    public abstract boolean takePermits(String key, Integer permits);

    public abstract void releasePermit(String key);

    public abstract void releasePermits(String key, Integer permits);

    protected Map<String, Integer> parse(String config) {

        if (StringUtils.isEmpty(config)) {
            return ImmutableMap.of();
        }

        Map<String, Integer> limiters = Maps.newHashMap();
        for (String keyValuePair : Constants.SPLIT_COMMA.splitToList(config)) {
            List<String> keyOrValue = Splitter.on(":").trimResults().splitToList(keyValuePair);
            try {
                int limit = Integer.parseInt(keyOrValue.get(1));
                // 只有 > 0时，才判别是有效。
                if (limit > 0) {
                    limiters.put(keyOrValue.get(0), limit);
                }
            } catch (Exception e) {
                // 如果解析失败，ignore
            }
        }
        return ImmutableMap.copyOf(limiters);
    }
}
