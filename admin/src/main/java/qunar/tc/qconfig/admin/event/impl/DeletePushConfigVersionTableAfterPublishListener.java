package qunar.tc.qconfig.admin.event.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.event.CandidateDTOChangeListener;
import qunar.tc.qconfig.admin.event.CandidateDTONotifyBean;
import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.service.PushConfigVersionService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 17:49
 */
@Service
public class DeletePushConfigVersionTableAfterPublishListener implements CandidateDTOChangeListener {

    @Resource
    private PushConfigVersionService pushConfigVersionService;

    @Override
    public void candidateDTOChanged(CandidateDTONotifyBean notifyBean) {
        if (notifyBean.event == ConfigOperationEvent.PUBLISH) {
            CandidateDTO candidateDTO = notifyBean.candidateDTO;
            ConfigMeta meta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
            pushConfigVersionService.asyncDelete(meta, candidateDTO.getEditVersion());
        }
    }
}
