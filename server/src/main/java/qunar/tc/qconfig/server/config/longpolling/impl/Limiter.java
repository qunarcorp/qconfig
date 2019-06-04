package qunar.tc.qconfig.server.config.longpolling.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/4/17 16:10
 */
class Limiter {

    private final int interval;

    private final int limit;

    private final Map<Integer, Integer> map;

    public Limiter(LimitInfo limitInfo) {
        this.interval = limitInfo.getIntervalSecond();
        this.limit = limitInfo.getLimitCount();
        this.map = Maps.newHashMapWithExpectedSize(interval + 1);
    }

    public synchronized boolean tryAcquire(int key) {
        addCount(key);
        int count = getIntervalCount(key);
        clear(key);
        return count <= limit;
    }

    private void addCount(int key) {
        int count = getCount(key);
        setCount(key, count + 1);
    }

    private int getIntervalCount(int key) {
        int count = 0;
        for (int i = key - interval + 1; i <= key; ++i) {
            count += getCount(i);
        }
        return count;
    }

    private int getCount(int key) {
        Integer count = map.get(key);
        return count != null ? count : 0;
    }

    private void setCount(int key, int count) {
        map.put(key, count);
    }

    private void clear(int key) {
        if (map.size() > interval) {
            int minKey = key - interval + 1;
            List<Integer> toClears = Lists.newArrayList();
            for (Integer k : map.keySet()) {
                if (k < minKey) {
                    toClears.add(k);
                }
            }

            for (Integer toClear : toClears) {
                map.remove(toClear);
            }
        }
    }
}
