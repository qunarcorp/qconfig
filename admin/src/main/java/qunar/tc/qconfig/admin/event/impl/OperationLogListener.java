package qunar.tc.qconfig.admin.event.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.dao.ReferenceLogDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.event.*;
import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.model.ReferenceLog;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author zhenyu.nie created on 2014 2014/5/26 19:09
 */
@Service
public class OperationLogListener implements CandidateDTOChangeListener, CandidateDTOPushListener, PublicStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConfigOpLogDao logDao;

    @Resource
    private ReferenceLogDao referenceLogDao;

    private Executor executor = Executors.newFixedThreadPool(2, new NamedThreadFactory("qconfig-oplog"));

    @Override
    public void candidateDTOChanged(CandidateDTONotifyBean candidateDTONotifyBean) {
        if (candidateDTONotifyBean == null) {
            return;
        }
        final CandidateDTONotifyBean notifyBean = candidateDTONotifyBean.copy();
        final CandidateDTO candidateDTO = notifyBean.candidateDTO;
        if (candidateDTO == null) {
            return;
        }

        // todo: 这个逻辑似乎不应该在这里进行修改
        final long basedVersion = candidateDTO.getStatus() == StatusType.PUBLISH ?
                candidateDTO.getEditVersion() : candidateDTO.getBasedVersion();

        candidateDTO.setEditVersion(candidateDTO.getEditVersion() + 1);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    logDao.insert(new ConfigOpLog(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile(),
                            basedVersion, candidateDTO.getEditVersion(), notifyBean.operator, notifyBean.event,
                            notifyBean.remarks, notifyBean.ip));

                    if (StringUtils.isNotBlank(candidateDTO.getInheritGroupId())
                            && StringUtils.isNotBlank(candidateDTO.getInheritDataId())) {//继承操作日志
                        referenceLogDao.create(new ReferenceLog(candidateDTO.getGroup(), candidateDTO.getProfile(), candidateDTO.getDataId(),
                                candidateDTO.getInheritGroupId(), candidateDTO.getInheritProfile(), candidateDTO.getInheritDataId(),
                                notifyBean.operator, RefChangeType.INHERIT, null));
                    }
                } catch (Exception e) {
                    logger.error("record operation log error, {}", notifyBean, e);
                }
            }
        });
    }

    @Override
    public void candidateDTOChanged(CandidateDTOPushNotifyBean notifyBean) {
        CandidateDTO dto = notifyBean.candidateDTONotifyBean.candidateDTO;
        try {
            if (notifyBean.candidateDTONotifyBean.event == ConfigOperationEvent.PUSH) {

                logDao.insert(new ConfigOpLog(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getBasedVersion(),
                        dto.getEditVersion(), notifyBean.candidateDTONotifyBean.operator,
                        notifyBean.candidateDTONotifyBean.event, generateRemarks(notifyBean),
                        notifyBean.candidateDTONotifyBean.ip));
            }
        } catch (Exception e) {
            logger.error("record operation log error, {}", notifyBean, e);
        }
    }


    @Override
    public void publicStatusChanged(final PublicStatusNotifyBean notifyBean) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    logDao.insert(new ConfigOpLog(notifyBean.configMeta.getGroup(), notifyBean.configMeta.getDataId(),
                            notifyBean.configMeta.getProfile(), notifyBean.basedVersion, notifyBean.currentVersion,
                            notifyBean.operator, notifyBean.event, notifyBean.remarks, notifyBean.ip));
                } catch (Exception e) {
                    logger.error("record public status change error, {}", notifyBean, e);
                }
            }
        });
    }

    protected String generateRemarks(CandidateDTOPushNotifyBean notifyBean) {
        StringBuilder sb = new StringBuilder();
        sb.append("目标机器：");
        for (int i = 0; i < notifyBean.destinations.size(); ++i) {
            PushItemWithHostName address = notifyBean.destinations.get(i);
            if (i != 0) {
                sb.append(",");
            }
            sb.append(address.getHostname());
            sb.append("(");
            sb.append(address.getIp());
            sb.append("):");
            sb.append(address.getPort());
        }
        sb.append(". ");
        sb.append(notifyBean.candidateDTONotifyBean.remarks);
        return sb.toString();
    }

}
