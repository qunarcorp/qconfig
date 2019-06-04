package qunar.tc.qconfig.server.feature;

import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Set;

/**
 * Created by dongcao on 2018/9/27.
 */
public interface OnlineClientListService {

    void register(ConfigMeta meta, String ip, long version);

    Set<String> getListeningClients(ConfigMeta meta);

    Set<ClientData> getListeningClientsData(ConfigMeta meta);

}
