package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 13:44
 */
public interface ReferenceChangeListener {

    @Subscribe
    void referenceChange(ReferenceNotifyBean notifyBean);
}
