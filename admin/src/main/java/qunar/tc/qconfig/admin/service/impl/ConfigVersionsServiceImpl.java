package qunar.tc.qconfig.admin.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.service.ConfigVersionsService;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author keli.wang
 * @since 2017/11/14
 */
@Service
public class ConfigVersionsServiceImpl implements ConfigVersionsService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigVersionsServiceImpl.class);

    private static final String CONFIG_HISTORY_LIMIT = "configHistory.limit";

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    private volatile int limit = 20;

    public ConfigVersionsServiceImpl() {
        final MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(conf -> {
            if (conf.containsKey(CONFIG_HISTORY_LIMIT)) {
                limit = Integer.parseInt(conf.get(CONFIG_HISTORY_LIMIT));
            }
            LOG.info("current config history limit is {}", limit);
        });
    }

    @Override
    public List<CandidateSnapshot> recentPublishedSnapshots(ConfigMeta configMeta) {
        return candidateSnapshotDao.findCandidateSnapshots(configMeta, StatusType.PUBLISH, limit);
    }
}
