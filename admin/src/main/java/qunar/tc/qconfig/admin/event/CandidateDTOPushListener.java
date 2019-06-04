package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * @author zhenyu.nie created on 2014 2014/6/18 12:34
 */
public interface CandidateDTOPushListener {

    @Subscribe
    void candidateDTOChanged(CandidateDTOPushNotifyBean notifyBean);
}
