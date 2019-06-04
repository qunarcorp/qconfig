package qunar.tc.qconfig.client;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-14.
 */
public class Feature {

    public static final Feature DEFAULT = Feature.create().build();

    public static final class Builder {

        // 是否自动载入
        private boolean autoReload = true;

        // 能容忍的最低版本限度
        private long minimumVersion = 0;

        private boolean failOnNotExists = true;

        private boolean trimValue = true;

        //是否采用https访问，默认关闭
        private boolean httpsEnable = false;

        public Builder autoReload(boolean autoReload) {
            this.autoReload = autoReload;
            return this;
        }

        public Builder setFailOnNotExists(boolean failOnNotExists) {
            this.failOnNotExists = failOnNotExists;
            return this;
        }

        public Builder minimumVersion(long minimumVersion) {
            this.minimumVersion = minimumVersion;
            return this;
        }

        public Builder setTrimValue(boolean trimValue) {
            this.trimValue = trimValue;
            return this;
        }

        public Builder setHttpsEnable(boolean httpsEnable) {
            this.httpsEnable = httpsEnable;
            return this;
        }

        public Feature build() {
            return new Feature(autoReload, minimumVersion, failOnNotExists, trimValue, httpsEnable);
        }
    }

    public static Builder create() {
        return new Builder();
    }

    // 是否自动载入
    private final boolean autoReload;

    // 能容忍的最低版本限度
    private final long minimumVersion;

    // 允许载入不存在的文件, 当文件存在时自动生效.
    private final boolean failOnNotExists;

    private final boolean trimValue;

    //是否采用https访问，默认关闭
    private final boolean httpsEnable;

    private Feature(boolean autoReload, long minimumVersion, boolean failOnNotExists, boolean trimValue, boolean httpsEnable) {
        this.autoReload = autoReload;
        this.minimumVersion = minimumVersion;
        this.failOnNotExists = failOnNotExists;
        this.trimValue = trimValue;
        this.httpsEnable = httpsEnable;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public long getMinimumVersion() {
        return minimumVersion;
    }

    public boolean isFailOnNotExists() {
        return failOnNotExists;
    }

    public boolean isTrimValue() {
        return trimValue;
    }

    public boolean isHttpsEnable() {
        return httpsEnable;
    }
}
