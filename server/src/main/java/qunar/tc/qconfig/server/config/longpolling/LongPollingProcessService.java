package qunar.tc.qconfig.server.config.longpolling;

import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.AsyncContext;
import java.util.List;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2017 2017/3/27 20:36
 */
public interface LongPollingProcessService {

    void process(AsyncContext context, List<CheckRequest> requests);

    Set<String> getListeningClients(ConfigMeta meta);

    Set<ClientData> getListeningClientsData(ConfigMeta meta);
}
