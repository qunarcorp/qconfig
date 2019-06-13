package qunar.tc.qconfig.client.util;

import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.util.FileUtil;

import java.io.File;

/**
 * @author keli.wang
 * @since 2017/4/18
 */
public class AppStoreUtil {
    private static final ServerManagement SERVER_MANAGER = ServerManager.getInstance();

    public static File getAppStore() {
        final String app = SERVER_MANAGER.getAppServerConfig().getName();
        return defaultAppStore(app);
    }


    private static File defaultAppStore(final String app) {
        return new File(FileUtil.getFileStore(), app);
    }
}
