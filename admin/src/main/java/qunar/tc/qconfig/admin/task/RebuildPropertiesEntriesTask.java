package qunar.tc.qconfig.admin.task;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.RebuildPropertiesEntryService;
import qunar.tc.qconfig.admin.service.RebuildPropertiesEntryService.ProgressListener;

import java.util.concurrent.TimeUnit;

/**
 * @author keli.wang
 */
@Component
public class RebuildPropertiesEntriesTask {

    @Resource
    private ConfigDao configDao;

    @Resource
    private RebuildPropertiesEntryService rebuildPropertiesEntryService;

    public void rebuildPropertiesEntries() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            configDao.countCurrentSnapshots();
            rebuildPropertiesEntryService.rebuildPropertiesEntry(new ProgressListener() {
                @Override
                public void onProgress(CandidateSnapshot snapshot) {
                }
            });
        } finally {
            Monitor.REBUILD_PROPERTIES_ENTRIES_TIMER.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
