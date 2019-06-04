package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.config.longpolling.impl.ReturnAction;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhenyu.nie created on 2018 2018/4/13 17:22
 */
public class InheritListener implements Listener {

    private final ConfigMeta meta;

    private final long version;

    private final AsyncContextHolder contextHolder;

    public InheritListener(ConfigMeta meta, AsyncContextHolder contextHolder, long version) {
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
        contextHolder.completeRequest(ACTION);
    }

    private static final ReturnAction ACTION = new ReturnAction() {
        @Override
        public String type() {
            return "inherit changes";
        }

        @Override
        public void act(AsyncContext context) throws Exception {
            ((HttpServletResponse) context.getResponse()).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
    };
}
