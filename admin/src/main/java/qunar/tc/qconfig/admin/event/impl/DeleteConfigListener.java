package qunar.tc.qconfig.admin.event.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.event.CurrentConfigChangedListener;
import qunar.tc.qconfig.admin.event.CurrentConfigNotifyBean;
import qunar.tc.qconfig.admin.service.UserBehaviorService;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;


@Service
public class DeleteConfigListener implements CurrentConfigChangedListener {

    private final Logger logger = LoggerFactory.getLogger(DeleteConfigListener.class);

    @Resource
    private UserBehaviorService userBehaviorService;

    @Override
    public void currentConfigChanged(CurrentConfigNotifyBean notifyBean) {
        if (notifyBean.getEvent() == ConfigOperationEvent.DELETE) {
            deleteUserFavorites(notifyBean);
        }
    }

    private void deleteUserFavorites(CurrentConfigNotifyBean notifyBean) {
        try {
            CandidateSnapshot snapshot = notifyBean.getSnapshot();
            userBehaviorService.deleteFavorites(new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile()));
        } catch (Exception e) {
            logger.error("delete user favorites error, notifyBean={}", notifyBean, e);
        }
    }
}
