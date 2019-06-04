package qunar.tc.qconfig.server.serverself.eureka;

import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/8/10 14:56
 */
public interface ServerStore {

    QConfigServer self();

    String defaultRoom();

    String getRoom(String ip);

    List<QConfigServer> getServers();

    List<QConfigServer> getAvailableServers();
}
