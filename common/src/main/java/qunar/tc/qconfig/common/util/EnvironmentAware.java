package qunar.tc.qconfig.common.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServerManager;

/**
 * User: zhaohuiyu
 * Date: 5/8/14
 * Time: 6:32 PM
 */
public class EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentAware.class);

    private static final String ENV_FILE = "qconfig.profile";

    private static final ServerManagement serverManager = ServerManager.getInstance();
    private static final EnvironmentAware INSTANCE = new EnvironmentAware();
    private final String profile;

    private EnvironmentAware() {
        this.profile = extractProfile();
    }

    public static String determinedEnv() {
        return INSTANCE.profile;
    }

    private String extractProfile() {
        String profileProperty = System.getProperty(ENV_FILE);
        if (!Strings.isNullOrEmpty(profileProperty)) {
            return profileProperty;
        }

        return getProfileFromCommon();
    }

    private String getProfileFromCommon() {
        try {
            return Strings.nullToEmpty(serverManager.getAppServerConfig().getSubEnv());
        } catch (NoSuchMethodError e) {
            logger.error("获取环境失败, 请在配置文件中配置");
            throw new NoSuchMethodError("请在配置文件中配置");
        }
    }
}
