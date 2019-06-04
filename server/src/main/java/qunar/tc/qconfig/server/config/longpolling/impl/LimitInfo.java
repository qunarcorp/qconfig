package qunar.tc.qconfig.server.config.longpolling.impl;

/**
 * @author zhenyu.nie created on 2018 2018/4/19 13:48
 */
public class LimitInfo {

    private final int intervalSecond;

    private final int limitCount;

    public LimitInfo(int intervalSecond, int limitCount) {
        this.intervalSecond = intervalSecond;
        this.limitCount = limitCount;
    }

    public int getIntervalSecond() {
        return intervalSecond;
    }

    public int getLimitCount() {
        return limitCount;
    }

    @Override
    public String toString() {
        return "LimitInfo{" +
                "intervalSecond=" + intervalSecond +
                ", limitCount=" + limitCount +
                '}';
    }
}
