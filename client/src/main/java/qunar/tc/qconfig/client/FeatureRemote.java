package qunar.tc.qconfig.client;

import qunar.tc.qconfig.common.codec.Codec;

/**
 * 远端feature
 * <p>
 * 是否本地缓存，解码算法
 * <p>
 * Created by chenjk on 2017/8/31.
 */
public class FeatureRemote {

    public static final FeatureRemote DEFAULT = new FeatureRemote(null, true);

    private Codec code;

    private boolean isLocalCache;

    private FeatureRemote(Codec code, boolean isLocalCache) {
        this.code = code;
        this.isLocalCache = isLocalCache;
    }

    public static final class Builder {

        private FeatureRemote featureRemote = new FeatureRemote(null, true);

        public Builder setCodec(Codec codec) {
            featureRemote.setCode(codec);
            return this;
        }

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

    public Codec getCode() {
        return code;
    }

    public boolean isLocalCache() {
        return isLocalCache;
    }

    public void setCode(Codec code) {
        this.code = code;
    }

    public void setLocalCache(boolean localCache) {
        isLocalCache = localCache;
    }
}
