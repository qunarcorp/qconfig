package qunar.tc.qconfig.server.serverself.eureka;

import com.google.common.collect.ImmutableList;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.bean.AppServerConfig;

import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/11/24 16:18
 */
public class TrivialServerStore implements ServerStore {

    private static final AppServerConfig appServer = ServerManager.getInstance().getAppServerConfig();

    private static final QConfigServer self = new QConfigServer(appServer.getIp(), appServer.getPort(), appServer.getRoom());

    private static final List<QConfigServer> servers = ImmutableList.of(self);

    @Override
    public QConfigServer self() {
        return self;
    }

    @Override
    public String defaultRoom() {
        return self().getRoom();
    }

    @Override
    public String getRoom(String ip) {
        return self().getRoom();
    }

    @Override
    public List<QConfigServer> getServers() {
        return servers;
    }

    @Override
    public List<QConfigServer> getAvailableServers() {
        return servers;
    }
}
