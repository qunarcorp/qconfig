package qunar.tc.qconfig.admin.event.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.event.CurrentConfigChangedListener;
import qunar.tc.qconfig.admin.event.CurrentConfigNotifyBean;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.service.PropertiesEntryService;
import qunar.tc.qconfig.common.util.FileChecker;

/**
 * @author keli.wang
 */
@Service
public class UpdatePropertiesEntryListener implements CurrentConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePropertiesEntryListener.class);

    @Resource
    private PropertiesEntryService propertiesEntryService;

    @Override
    public void currentConfigChanged(CurrentConfigNotifyBean notifyBean) {
        LOG.info("current config changed. notifyBean = {}", notifyBean);

        if (!FileChecker.isPropertiesFile(notifyBean.getSnapshot().getDataId())) {
            return;
        }

        try {
            if (notifyBean.getEvent() == ConfigOperationEvent.PUBLISH) {
                propertiesEntryService.saveEntries(notifyBean.getSnapshot());
            } else if (notifyBean.getEvent() == ConfigOperationEvent.DELETE) {
                propertiesEntryService.removeEntries(notifyBean.getSnapshot());
            }
        } catch (ModifiedException e) {
            LOG.warn("failed generating PropertiesEntries index. notifyBean={}",
                     notifyBean,
                     e);
        }
    }
}
