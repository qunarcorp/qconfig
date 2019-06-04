package qunar.tc.qconfig.admin.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.admin.service.PropertiesEntryService;
import qunar.tc.qconfig.admin.service.RebuildPropertiesEntryService;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author keli.wang
 */
@Service
public class RebuildPropertiesEntryServiceImpl implements RebuildPropertiesEntryService {
    private static final Logger LOG = LoggerFactory.getLogger(RebuildPropertiesEntryServiceImpl.class);

    // 一次取出500行
    private static final int BATCH_FETCH_SIZE = 500;

    // rebuild重试间隔，单位为ms
    private static final int RETRY_INTERVAL_IN_MS = 30;
    // rebuild最多重试的次数
    private static final int MAX_RETRY_TIMES = 3;

    @Resource
    private ConfigDao configDao;

    @Resource
    private PropertiesEntryService propertiesEntryService;

    private CandidateSnapshot reloadSnapshot(final CandidateSnapshot snapshot) {
        final ConfigMeta configMeta = new ConfigMeta(snapshot.getGroup(),
                snapshot.getData(),
                snapshot.getProfile());
        return configDao.findCurrentSnapshot(configMeta);
    }

    private CandidateSnapshot reloadAndRebuild(final CandidateSnapshot snapshot,
                                               final int maxRetryTimes) {
        // 超过最大重试次数
        if (maxRetryTimes == 0) {
            return snapshot;
        }
        try {
            // 等待一定时间
            TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_IN_MS);

            final CandidateSnapshot newSnapshot = reloadSnapshot(snapshot);
            propertiesEntryService.handleSnapshotByStatus(newSnapshot);
            return newSnapshot;
        } catch (Exception e) {
            LOG.warn("error occurred while reload and rebuild. snapshot={}, maxRetryTime={}",
                    snapshot,
                    maxRetryTimes,
                    e);

            return reloadAndRebuild(snapshot, maxRetryTimes - 1);
        }
    }

    private CandidateSnapshot rebuildFor(final CandidateSnapshot snapshot) {
        try {
            propertiesEntryService.handleSnapshotByStatus(snapshot);
        } catch (ModifiedException e) {
            LOG.warn("ModifiedException occurred while rebuild PropertiesEntry for {}", snapshot, e);
            return reloadAndRebuild(snapshot, MAX_RETRY_TIMES);
        } catch (Exception e) {
            LOG.error("unknown exception occurred. snapshot={}", snapshot, e);
        }

        return snapshot;
    }

    @Override
    public void rebuildPropertiesEntry(final ProgressListener listener) {
        int offset = 0;
        int fetchSize;
        do {
            final List<CandidateSnapshot> snapshots = configDao.findCurrentSnapshots(offset, BATCH_FETCH_SIZE);
            fetchSize = snapshots.size();
            offset += fetchSize;
            for (CandidateSnapshot snapshot : snapshots) {
                if (FileChecker.isPropertiesFile(snapshot.getDataId())) {
                    listener.onProgress(rebuildFor(snapshot));
                }
            }
        } while (fetchSize == BATCH_FETCH_SIZE);
    }
}
