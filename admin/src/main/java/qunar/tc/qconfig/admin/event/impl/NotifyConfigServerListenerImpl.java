package qunar.tc.qconfig.admin.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.*;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-26
 * Time: 下午2:33
 */
@Service
public class NotifyConfigServerListenerImpl implements CandidateDTOChangeListener, ReferenceChangeListener, PublicStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private NotifyService notifyService;

    @Override
    public void candidateDTOChanged(CandidateDTONotifyBean notifyBean) {
        notifyBean = notifyBean.copy();
        if (notifyBean.event == ConfigOperationEvent.PUBLISH || notifyBean.event == ConfigOperationEvent.DELETE) {
            try {
                notifyService.notify(notifyBean.candidateDTO.getGroup(), notifyBean.candidateDTO.getDataId(),
                        notifyBean.candidateDTO.getProfile());
            } catch (Exception e) {
                logger.error("notify candidate dto error, {}", notifyBean, e);
            }
        }
    }

    // todo: 要不要在push时notify
    @Override
    public void referenceChange(ReferenceNotifyBean notifyBean) {
        try {
            notifyService.notifyReference(notifyBean.reference, notifyBean.changeType);
        } catch (Exception e) {
            logger.error("notify reference error, {}", notifyBean, e);
        }
    }

    @Override
    public void publicStatusChanged(PublicStatusNotifyBean notifyBean) {
        try {
            ConfigMetaWithoutProfile meta = new ConfigMetaWithoutProfile(notifyBean.configMeta.getGroup(), notifyBean.configMeta.getDataId());
            notifyService.notifyPublic(meta);
        } catch (Exception e) {
            logger.error("notify public file error, {}", notifyBean, e);
        }
    }
}
