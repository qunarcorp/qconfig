package qunar.tc.qconfig.admin.greyrelease;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.service.GreyReleaseService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.admin.service.PushConfigVersionService;
import qunar.tc.qconfig.admin.service.PushHistoryService;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2018 2018/5/23 11:58
 */
@Service
public class ReleaseStatusFactoryImpl implements ReleaseStatusFactory {

    @Resource
    private NotifyService notifyService;

    @Resource
    private GreyReleaseService greyReleaseService;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private PushConfigVersionService pushConfigVersionService;

    @Resource
    private ConfigService configService;

    @Resource
    private PushHistoryService pushHistoryService;

    @Resource
    private ListeningClientsService listeningClientsService;

    @Override
    public ReleaseStatus create(StatusInfo statusInfo) {
        AbstractReleaseStatus abstractReleaseStatus;
        switch (statusInfo.getState()) {
            case WAIT_PUBLISH:
                abstractReleaseStatus = new WaitPublishReleaseStatus(statusInfo, this, notifyService);
                break;
            case DELAY_PUBLISH:
                abstractReleaseStatus = new DelayPublishReleaseStatus(statusInfo, this, notifyService, configUsedLogDao, listeningClientsService);
                break;
            case PUBLISHING:
                abstractReleaseStatus = new PublishingReleaseStatus(statusInfo, this, notifyService);
                break;
            case FINISH:
                abstractReleaseStatus = new FinishedReleaseStatus(statusInfo, this, notifyService);
                break;
            case CANCEL:
                abstractReleaseStatus = new CancelReleaseStatus(statusInfo, this, notifyService);
                break;
            default:
                throw new IllegalArgumentException("unknown status, " + statusInfo);
        }
        abstractReleaseStatus.setPushHistoryService(pushHistoryService);
        abstractReleaseStatus.setGreyReleaseService(greyReleaseService);
        abstractReleaseStatus.setPushConfigVersionService(pushConfigVersionService);
        abstractReleaseStatus.setConfigService(configService);
        return abstractReleaseStatus;
    }
}
