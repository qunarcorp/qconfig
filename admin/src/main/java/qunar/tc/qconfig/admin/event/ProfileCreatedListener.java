package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 23:03
 */
public interface ProfileCreatedListener {

    @Subscribe
    void profileCreated(ProfileCreatedBean profileCreatedBean);
}
