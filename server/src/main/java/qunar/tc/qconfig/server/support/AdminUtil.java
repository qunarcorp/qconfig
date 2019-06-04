package qunar.tc.qconfig.server.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import qunar.tc.qconfig.client.MapConfig;

/**
 * @author zhenyu.nie created on 2018 2018/5/29 17:55
 */
public class AdminUtil {

    private static volatile String adminApp;

    static {
        adminApp = MapConfig.get("config.properties").asMap().get("push.app");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(adminApp));
    }

    public static String getAdminApp() {
        return adminApp;
    }
}
