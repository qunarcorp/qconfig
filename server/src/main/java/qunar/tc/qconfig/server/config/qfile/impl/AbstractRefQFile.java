package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/11/3 16:55
 */
public abstract class AbstractRefQFile extends AbstractDelegatedQFile implements QFile {

    private ConfigMeta source;

    public AbstractRefQFile(ConfigMeta source, QFile delegate) {
        super(delegate);
        this.source = source;
    }

    @Override
    public ConfigMeta getSourceMeta() {
        return source;
    }

    @Override
    public ConfigMeta getSharedMeta() {
        return source;
    }
}
