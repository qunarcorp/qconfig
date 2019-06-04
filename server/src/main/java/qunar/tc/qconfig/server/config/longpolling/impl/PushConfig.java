package qunar.tc.qconfig.server.config.longpolling.impl;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 18:05
 */
public class PushConfig {

    private final int pushMax;

    private final long pushInterval;

    private final int directPushLimit;

    public PushConfig(int pushMax, long pushInterval, int directPushLimit) {
        this.pushMax = pushMax;
        this.pushInterval = pushInterval;
        this.directPushLimit = directPushLimit;
    }

    public int getPushMax() {
        return pushMax;
    }

    public long getPushInterval() {
        return pushInterval;
    }

    public int getDirectPushLimit() {
        return directPushLimit;
    }

    @Override
    public String toString() {
        return "PushConfig{" +
                "pushMax=" + pushMax +
                ", pushInterval=" + pushInterval +
                ", directPushLimit=" + directPushLimit +
                '}';
    }
}
