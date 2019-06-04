package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.config.longpolling.impl.ChangeReturnAction;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/4/13 18:57
 */
public class NormalListener implements Listener {

    private final ConfigMeta meta;

    private final AsyncContextHolder contextHolder;

    private final long version;

    public NormalListener(ConfigMeta meta, AsyncContextHolder contextHolder, long version) {
        this.meta = meta;
        this.contextHolder = contextHolder;
        this.version = version;
    }

    @Override
    public ConfigMeta getMeta() {
        return meta;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public AsyncContextHolder getContextHolder() {
        return contextHolder;
    }

    @Override
    public void onChange(Changed change, String type) {
        change = transformChange(change);
        contextHolder.completeRequest(new ChangeReturnAction(change.str(), type));
    }

    private Changed transformChange(Changed change) {
        if (meta.getDataId().equals(change.getDataId())) {
            return change;
        } else {
            return new Changed(change.getGroup(), meta.getDataId(), change.getProfile(), change.getNewestVersion());
        }
    }
}
