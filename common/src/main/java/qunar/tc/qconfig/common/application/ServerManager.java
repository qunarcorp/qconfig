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
import qunar.tc.qconfig.common.util.LocalHostUtil;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;


/**
 * miao.yang susing@gmail.com 2013-8-7
 */
public class ServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServerManager.class);

    private static ImmutableMap<String, String> appInfoMap = ImmutableMap.copyOf(ServerLocalContextLoader.loadLocalContext());

    private static final String ENV_NAME_KEY = "env";
    private static final String APP_CODE_KEY = "appCode";
    private static final String APP_PROFILE_KEY = "profile";
    private static final String APP_ROOM_KEY = "room";
    private static final String TOKEN_KEY = "token";
    private static final String PORT_KEY = "port";

    private static final AtomicReference<ServerManagement> INSTANCE = new AtomicReference<>();

    private static final Supplier<ServerManagement> holder = Suppliers.memoize(new Supplier<ServerManagement>() {
        @Override
        public ServerManagement get() {
            ServiceLoader<ServerManagement> managers = ServiceLoader.load(ServerManagement.class);

            ServerManagement instance = null;
            for (ServerManagement registry : managers) {
                instance = registry;
                break;
            }
            if (instance == null) {
                instance = new InnerServerManagerImpl();
            }

            INSTANCE.compareAndSet(null, instance);
            return instance;
        }
    });

    public static ServerManagement getInstance() {
        holder.get();
        return INSTANCE.get();
    }
    private volatile static AppServerConfig appConfig;

    private static class InnerServerManagerImpl implements ServerManagement {

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
                    appConfig = new AppServerConfig(appCode, env, token, ip, port, type, profile, Strings.nullToEmpty(null),  Strings.nullToEmpty(room));
                }
            }
            return appConfig;
        }
    }
}