package qunar.tc.qconfig.admin.event;

import qunar.tc.qconfig.admin.model.PushItemWithHostName;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/18 12:42
 */
public class CandidateDTOPushNotifyBean {

    public final CandidateDTONotifyBean candidateDTONotifyBean;

    public final List<PushItemWithHostName> destinations;

    public CandidateDTOPushNotifyBean(CandidateDTONotifyBean candidateDTONotifyBean, List<PushItemWithHostName> destinations) {
        this.candidateDTONotifyBean = candidateDTONotifyBean;
        this.destinations = destinations;
    }

    @Override
    public String toString() {
        return "CandidateDTOPushNotifyBean{" +
                "candidateDTONotifyBean=" + candidateDTONotifyBean +
                ", destinations=" + destinations +
                '}';
    }
}
