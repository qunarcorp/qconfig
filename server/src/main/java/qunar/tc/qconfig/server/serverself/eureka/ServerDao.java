package qunar.tc.qconfig.server.serverself.eureka;

import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/8/10 17:04
 */
public interface ServerDao {

    int insert(QConfigServer server);

    String selectRoom(String ip);

    List<QConfigServer> selectServers();
}
