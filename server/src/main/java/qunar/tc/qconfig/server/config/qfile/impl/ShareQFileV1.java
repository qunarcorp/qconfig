package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:56
 */
public class ShareQFileV1 extends AbstractShareQFile implements QFile {

    public ShareQFileV1(ConfigMeta source, QFile delegate) {
        super(source, delegate);
    }
}
