package qunar.tc.qconfig.server.serverself.eureka;

import com.google.common.base.Preconditions;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import qunar.tc.qconfig.common.application.ServerManager;

/**
 * 覆盖配置
 *
 * Created by chenjk on 2017/9/22.
 */
public class CustomDataCenterInstanceConfig extends MyDataCenterInstanceConfig {

    @Override
    public String getIpAddress() {
        return ServerManager.getInstance().getAppServerConfig().getIp();
    }

    @Override
    public String getHostName(boolean refresh) {
        return ServerManager.getInstance().getAppServerConfig().getIp();
    }

    @Override
    public int getNonSecurePort() {
        int port = ServerManager.getInstance().getAppServerConfig().getPort();
        Preconditions.checkArgument(port != 0, "port can not be 0");
        return port;
    }
}
