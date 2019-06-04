package qunar.tc.qconfig.common.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServiceFinder;

/**
 * Created by chenjk on 2018/3/14.
 */
public class EnvUtil {
    protected static final Logger logger = LoggerFactory.getLogger(EnvUtil.class);

    private static final ServerManagement serverManager;
    private static final String ENV;

    static {
        try {
            serverManager = ServiceFinder.getService(ServerManagement.class);
            ENV = serverManager.getAppServerConfig().getEnv().toLowerCase();
        } catch (Throwable e) {
            logger.error("init ServerManagement failed", e);
            throw new RuntimeException(e);
        }
    }

    public static ServerManagement getServerManager() {
        return serverManager;
    }

    public static boolean isPro() {
        return (!Strings.isNullOrEmpty(ENV) && ENV.contains("pro"));
    }
}
