package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.config.longpolling.impl.ChangeReturnAction;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/4/13 17:08
 */
public class ReturnMetaChangedListener implements Listener {

    private final ConfigMeta hangMeta;

    private final ConfigMeta returnMeta;

    private final AsyncContextHolder contextHolder;

    private final long version;

    public ReturnMetaChangedListener(ConfigMeta hangMeta, ConfigMeta returnMeta, AsyncContextHolder contextHolder, long version) {
        this.hangMeta = hangMeta;
        this.returnMeta = returnMeta;
        this.contextHolder = contextHolder;
        this.version = version;
    }

    @Override
    public ConfigMeta getMeta() {
        return hangMeta;
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
        return new Changed(returnMeta.getGroup(), returnMeta.getDataId(), returnMeta.getProfile(), change.getNewestVersion());
    }
}
