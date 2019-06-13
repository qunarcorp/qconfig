package qunar.tc.qconfig.client.impl;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.enums.AppServerType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.EnvironmentAware;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;

/**
 * @author zhenyu.nie created on 2014 2014/10/28 22:41
 */
public class VersionProfile {

    public static final Logger logger = LoggerFactory.getLogger(VersionProfile.class);

    public static final String LOCAL_PROFILE = getLocalProfile();

    public static final VersionProfile ABSENT = new VersionProfile(Constants.NO_FILE_VERSION, LOCAL_PROFILE);

    private final long version;

    private final String profile;

    VersionProfile(long version, String profile) {
        this.version = version;
        this.profile = profile;
    }

    public long getVersion() {
        return version;
    }

    public String getProfile() {
        return profile;
    }

    public boolean needUpdate(VersionProfile versionProfile) {
        if (versionProfile == null) {
            return false;
        }

        if (versionProfile.getVersion() <= Constants.NO_FILE_VERSION) {
            return false;
        }

        if (this == ABSENT) {
            return true;
        }

        if (this.profile.equals(versionProfile.profile) && this.version >= versionProfile.version) {
            return false;
        }

        return true;
    }

    private static String getLocalProfile() {
        String env = getEnv();
        if (Strings.isNullOrEmpty(env)) {
            logger.warn("无法获取当前机器环境");
            return "";
        }

        return env + ":" + EnvironmentAware.determinedEnv();
    }

    private static String getEnv() {
        try {
            return ServerManager.getInstance().getAppServerConfig().getEnv();
        } catch (NoSuchMethodError error) {
            return getOldPrefix();
        }
    }

    private static String getOldPrefix() {
        AppServerType type =ServerManager.getInstance().getAppServerConfig().getType();
        if (type == null) {
            return "";
        }

        String env = "";
        if (AppServerType.dev == type) {
            env = "dev";
        } else if (AppServerType.beta == type) {
            env = "beta";
        } else if (AppServerType.prod == type) {
            env = "prod";
        }

        return QConfigAttributesLoader.getInstance().getSymbol(env);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionProfile)) return false;

        VersionProfile that = (VersionProfile) o;

        if (version != that.version) return false;
        return !(profile != null ? !profile.equals(that.profile) : that.profile != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (version ^ (version >>> 32));
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VersionProfile{" +
                "version=" + version +
                ", profile='" + profile + '\'' +
                '}';
    }
}
