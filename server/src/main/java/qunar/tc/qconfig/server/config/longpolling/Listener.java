package qunar.tc.qconfig.server.config.longpolling;

import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 18:01
 */
public interface Listener {

    ConfigMeta getMeta();

    long getVersion();

    AsyncContextHolder getContextHolder();

    void onChange(Changed change, String type);
}
