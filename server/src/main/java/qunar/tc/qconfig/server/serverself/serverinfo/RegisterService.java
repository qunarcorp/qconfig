package qunar.tc.qconfig.server.serverself.serverinfo;

import qunar.tc.qconfig.server.serverself.eureka.QConfigServer;

import java.util.Set;

/**
 * User: zhaohuiyu
 * Date: 5/19/14
 * Time: 10:38 AM
 */
public interface RegisterService {

    Set<QConfigServer> list(final String room);

    Set<QConfigServer> all(final ClusterType clusterType);

    Set<QConfigServer> all();

    Set<QConfigServer> defaultServer();
}
