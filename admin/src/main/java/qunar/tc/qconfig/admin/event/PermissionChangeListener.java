package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 14:44
 */
public interface PermissionChangeListener {

    @Subscribe
    public void permissionChange(PermissionNotifyBean permissionNotifyBean);
}
