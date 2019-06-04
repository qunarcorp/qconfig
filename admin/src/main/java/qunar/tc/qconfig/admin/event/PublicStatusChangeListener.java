package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * @author zhenyu.nie created on 2014 2014/6/27 15:52
 */
public interface PublicStatusChangeListener {

    @Subscribe
    void publicStatusChanged(PublicStatusNotifyBean notifyBean);
}
