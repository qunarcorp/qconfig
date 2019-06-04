package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * 当前正式的Config改变时才会触发，即新的Config被发布和或是Config被删除了
 * @author keli.wang
 */
public interface CurrentConfigChangedListener {

    @Subscribe
    void currentConfigChanged(final CurrentConfigNotifyBean notifyBean);
}
