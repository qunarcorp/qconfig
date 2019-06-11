package qunar.tc.qconfig.client;


/**
 * 远端feature
 * <p>
 * 是否本地缓存，解码算法
 * <p>
 * Created by chenjk on 2017/8/31.
 */
public class FeatureRemote {

    public static final FeatureRemote DEFAULT = new FeatureRemote(true);

    private boolean isLocalCache;

    private FeatureRemote(boolean isLocalCache) {
        this.isLocalCache = isLocalCache;
    }

    public static final class Builder {

        private FeatureRemote featureRemote = new FeatureRemote(true);

        public Builder setIsLocalCache(boolean isLocalCache) {
            featureRemote.setLocalCache(isLocalCache);
            return this;
        }

        public FeatureRemote build() {
            return featureRemote;
        }
    }

    public static Builder create() {
        return new Builder();
    }

    public boolean isLocalCache() {
        return isLocalCache;
    }

    public void setLocalCache(boolean localCache) {
        isLocalCache = localCache;
    }
}
