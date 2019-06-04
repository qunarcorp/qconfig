package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/10/31 15:44
 */
public abstract class AbstractShareQFile extends AbstractDelegatedQFile {

    private ConfigMeta source;

    protected AbstractShareQFile(ConfigMeta source, QFile delegate) {
        super(delegate);
        this.source = source;
    }

    @Override
    public ConfigMeta getSourceMeta() {
        return source;
    }

    @Override
    public ConfigMeta getSharedMeta() {
        return delegate.getSourceMeta();
    }
}
