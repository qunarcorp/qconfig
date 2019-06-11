package qunar.tc.qconfig.common.application;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.bean.AppServerConfig;
import qunar.tc.qconfig.common.enums.AppServerType;
import qunar.tc.qconfig.common.support.ServerLocalContextLoader;
import qunar.tc.qconfig.common.support.TomcatStateViewer;
import qunar.tc.qconfig.common.util.LocalHostUtil;

import java.util.concurrent.atomic.AtomicReference;


/**
 * miao.yang susing@gmail.com 2013-8-7
 */
public class ServerManager implements ServerManagement{

    private static final Logger LOG = LoggerFactory.getLogger(ServerManager.class);

    private static ImmutableMap<String, String> appInfoMap = ImmutableMap.copyOf(ServerLocalContextLoader.loadLocalContext());

    private static final String ENV_NAME_KEY = "env";
    private static final String APP_CODE_KEY = "appCode";
    private static final String APP_PROFILE_KEY = "profile";
    private static final String APP_ROOM_KEY = "room";
    private static final String TOKEN_KEY = "token";
    private static final String PORT_KEY = "port";

    private static final Supplier<ServerManagement> holder = Suppliers.memoize(() -> ServiceFinder.getService(ServerManagement.class));
    private static final AtomicReference<ServerManager> instance = new AtomicReference<>();

    public static ServerManager getInstance() {
        holder.get();
        return instance.get();
    }
    private volatile static AppServerConfig appConfig;

    public ServerManager() {
        if (!instance.compareAndSet(null, this)) {
            throw new IllegalStateException("ServerManager只能被初始化一次.");
        }
    }

    @Override
    public boolean healthCheck() {
        return true;
    }

    @Override
    public AppServerConfig getAppServerConfig() {
        if (appConfig == null) {
            synchronized (ServerManager.class) {
                String appCode = Strings.nullToEmpty(appInfoMap.get(APP_CODE_KEY));
                String env = Strings.nullToEmpty(appInfoMap.get(ENV_NAME_KEY));
                int port = Integer.valueOf(appInfoMap.get(PORT_KEY));
                String ip = LocalHostUtil.getLocalHost();
                String profile = env + ":" + Strings.nullToEmpty(appInfoMap.get(APP_PROFILE_KEY));
                String room = Strings.nullToEmpty(appInfoMap.get(APP_ROOM_KEY));
                String token = Strings.nullToEmpty(appInfoMap.get(TOKEN_KEY));
                AppServerType type = AppServerType.valueOf(env);
                appConfig = new AppServerConfig(appCode, env, token, ip, port, type, profile, Strings.nullToEmpty(appInfoMap.get(APP_PROFILE_KEY)), room);
            }
        }
        return appConfig;
    }
}