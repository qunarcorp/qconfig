package qunar.tc.qconfig.server.config.apiversion;

import qunar.tc.qconfig.server.config.qfile.QFileFactory;

/**
 * @author zhenyu.nie created on 2017 2017/3/29 17:02
 */
public interface VersionChooseService {

    QFileFactory getQFileFactory();

    RequestParser getRequestParser();
}
