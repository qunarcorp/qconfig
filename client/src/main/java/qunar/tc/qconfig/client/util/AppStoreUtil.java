package qunar.tc.qconfig.client.util;

import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServiceFinder;
import qunar.tc.qconfig.common.util.FileUtil;

import java.io.File;

/**
 * @author keli.wang
 * @since 2017/4/18
 */
public class AppStoreUtil {
    private static final ServerManagement SERVER_MANAGER = ServiceFinder.getService(ServerManagement.class);

    public static File getAppStore() {
        final String app = SERVER_MANAGER.getAppServerConfig().getName();
        return defaultAppStore(app);
    }


    private static File defaultAppStore(final String app) {
        return new File(FileUtil.getFileStore(), app);
    }
}
