package qunar.tc.qconfig.admin.service;

import com.google.common.util.concurrent.ListenableFuture;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author zhenyu.nie created on 2018 2018/5/18 16:37
 */
public interface ListeningClientsService {

    Map<String, Set<ClientData>> getListeningPushStatus(ConfigMeta meta, long editVersion, List<String> ipPorts) throws InterruptedException, ExecutionException, TimeoutException;

    ListenableFuture<Set<Host>> getListeningClients(ConfigMeta meta);

    ListenableFuture<Set<ClientData>> getListeningClientsData(ConfigMeta meta, boolean needRemoveFixed);

    Optional<ClientData> getListeningClientsData(ConfigMeta meta, String ip) throws InterruptedException, ExecutionException, TimeoutException;

}
