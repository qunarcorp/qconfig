package qunar.tc.qconfig.admin.event.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.event.CurrentConfigChangedListener;
import qunar.tc.qconfig.admin.event.CurrentConfigNotifyBean;
import qunar.tc.qconfig.admin.service.PropertiesEntryLogService;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;


@Service
public class UpdatePropertiesEntryLogListener implements CurrentConfigChangedListener {

    private final Logger logger = LoggerFactory.getLogger(UpdatePropertiesEntryLogListener.class);

    @Resource
    private PropertiesEntryLogService propertiesEntryLogService;

    @Override
    public void currentConfigChanged(CurrentConfigNotifyBean notifyBean) {
        if (!FileChecker.isPropertiesFile(notifyBean.getSnapshot().getDataId())) {
            return;
        }
        CandidateSnapshot snapshot = notifyBean.getSnapshot();
        try {
            if (notifyBean.getEvent() == ConfigOperationEvent.PUBLISH) {
                propertiesEntryLogService.saveEntryLog(snapshot);
            } else if (notifyBean.getEvent() == ConfigOperationEvent.DELETE) {
                propertiesEntryLogService.deleteEntryLog(new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile()));
            }
        } catch (Exception e) {
            logger.error("update properties entries log error, notifyBean={}", notifyBean, e);
        }
    }
}
