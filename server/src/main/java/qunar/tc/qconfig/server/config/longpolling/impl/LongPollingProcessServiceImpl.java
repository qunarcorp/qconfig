package qunar.tc.qconfig.server.config.longpolling.impl;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.config.check.CheckResult;
import qunar.tc.qconfig.server.config.check.CheckService;
import qunar.tc.qconfig.server.config.check.CheckUtil;
import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.LongPollingProcessService;
import qunar.tc.qconfig.server.config.longpolling.LongPollingStore;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.config.qfile.impl.InheritQFileV2;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.feature.OnlineClientListService;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.web.servlet.AbstractCheckConfigServlet;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2017 2017/3/28 15:26
 */
@Service
public class LongPollingProcessServiceImpl implements LongPollingProcessService {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingProcessServiceImpl.class);

    @Resource(name = "v2Factory")
    private QFileFactory qFileFactory;

    @Resource
    private ClientInfoService clientInfoService;

    @Resource
    private CheckService checkService;

    @Resource
    private LongPollingStore longPollingStore;

    @Resource
    private OnlineClientListService onlineClientListService;

    private static final long DEFAULT_TIMEOUT = 60 * 1000L;

    private volatile long timeout = DEFAULT_TIMEOUT;

    @PostConstruct
    public void init() {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                String newTimeout = conf.get("longPolling.server.timeout");
                if (!Strings.isNullOrEmpty(newTimeout)) {
                    timeout = Numbers.toLong(newTimeout, DEFAULT_TIMEOUT);
                }
            }
        });
    }

    @Override
    public void process(AsyncContext context, List<CheckRequest> requests) {
        IpAndPort address = new IpAndPort(clientInfoService.getIp(), clientInfoService.getPort());
        AsyncContextHolder contextHolder = new AsyncContextHolder(context, address);
        context.setTimeout(timeout);
        context.addListener(new TimeoutServletListener(contextHolder));
        processCheckRequests(requests, clientInfoService.getIp(), contextHolder);
    }

    @Override
    public Set<String> getListeningClients(ConfigMeta meta) {
        return onlineClientListService.getListeningClients(meta);
    }

    @Override
    public Set<ClientData> getListeningClientsData(ConfigMeta meta) {
        return onlineClientListService.getListeningClientsData(meta);
    }

    private void processCheckRequests(List<CheckRequest> requests, String ip, AsyncContextHolder contextHolder) {
        CheckResult result = checkService.check(requests, ip, qFileFactory);
        logger.info("profile:{}, result change list {} for check request {}", clientInfoService.getProfile(), result.getChanges(), requests);

        if (!result.getChanges().isEmpty()) {
            returnChanges(AbstractCheckConfigServlet.formatOutput(CheckUtil.processStringCase(result.getChanges())), contextHolder, Constants.UPDATE);
            return;
        }

        addListener(result.getRequestsNoChange(), contextHolder);
        registerOnlineClients(result, contextHolder);
    }

    private void addListener(Map<CheckRequest, QFile> requests, AsyncContextHolder contextHolder) {
        for (Map.Entry<CheckRequest, QFile> noChangeEntry : requests.entrySet()) {
            CheckRequest request = noChangeEntry.getKey();
            QFile qFile = noChangeEntry.getValue();
            if (!contextHolder.isComplete()) {
                Listener listener = qFile.createListener(request, contextHolder);
                longPollingStore.addListener(listener);
            }
        }
    }

    private void registerOnlineClients(CheckResult result, AsyncContextHolder contextHolder) {
        Map<CheckRequest, QFile> noChanges = Maps.newHashMapWithExpectedSize(
                result.getRequestsNoChange().size() + result.getRequestsLockByFixVersion().size());
        noChanges.putAll(result.getRequestsNoChange());
        noChanges.putAll(result.getRequestsLockByFixVersion());

        for (Map.Entry<CheckRequest, QFile> noChangeEntry : noChanges.entrySet()) {
            CheckRequest request = noChangeEntry.getKey();
            QFile qFile = noChangeEntry.getValue();
            if (!contextHolder.isComplete()) {
                long version = request.getVersion();
                ConfigMeta meta = qFile.getRealMeta();
                String ip = contextHolder.getIp();
                if (qFile instanceof InheritQFileV2) {
                    InheritQFileV2 inheritQFile = (InheritQFileV2) qFile;
                    Optional<Long> optional = inheritQFile.getCacheConfigInfoService().getVersion(inheritQFile.getRealMeta());
                    version = optional.isPresent() ? optional.get() : version;
                    onlineClientListService.register(inheritQFile.getRealMeta(), ip, version);
                } else {
                    onlineClientListService.register(meta, ip, version);
                }
            }
        }
    }

    private void returnChanges(String change, AsyncContextHolder contextHolder, String type) {
        contextHolder.completeRequest(new ChangeReturnAction(change, type));
    }
}
