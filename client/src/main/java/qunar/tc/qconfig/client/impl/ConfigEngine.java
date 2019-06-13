package qunar.tc.qconfig.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.DataLoader;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.bean.AppServerConfig;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-12.
 */
public class ConfigEngine extends AbstractDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigEngine.class);

    private static final ConfigEngine instance = new ConfigEngine();

    public static DataLoader getInstance() {
        return instance;
    }

    private String appName;

    private ConfigEngine() {
    }

    @Override
    public String getGroupName() {
        try {
            if (appName != null) return appName;
            AppServerConfig appConfig = ServerManager.getInstance().getAppServerConfig();
            appName = appConfig.getName();
            return appName;
        } catch (Throwable e) {
            String message = "请确保在正确的位置配置了app id";
            logger.error(message, e);
            throw new RuntimeException(message);
        }
    }
}
