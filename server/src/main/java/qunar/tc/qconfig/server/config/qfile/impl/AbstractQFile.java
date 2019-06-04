package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/10/31 15:51
 */
public abstract class AbstractQFile implements QFile {

    @Override
    public Listener createListener(CheckRequest request, AsyncContextHolder contextHolder) {
        ConfigMeta hangMeta = getRealMeta();
        ConfigMeta returnMeta = getReturnMeta(request);
        if (hangMeta.equals(returnMeta)) {
            return new NormalListener(returnMeta, contextHolder, request.getVersion());
        } else {
            return new ReturnMetaChangedListener(hangMeta, returnMeta, contextHolder, request.getVersion());
        }
    }

    @Override
    public void log(Log log) {
        getLogService().log(log, getSharedMeta(), getRealMeta());
    }

    private ConfigMeta getReturnMeta(CheckRequest request) {
        ConfigMeta sharedMeta = getSharedMeta();
        if (request.getDataId().equals(sharedMeta.getDataId())) {
            return sharedMeta;
        } else {
            return new ConfigMeta(sharedMeta.getGroup(), request.getDataId(), sharedMeta.getProfile());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "sourceMeta=" + getSourceMeta() +
                ", shareMeta=" + getSharedMeta() +
                ", realMeta=" + getRealMeta() +
                '}';
    }
}
