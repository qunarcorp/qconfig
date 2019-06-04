package qunar.tc.qconfig.server.support.log;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 14:57
 */
public interface LogService {

    void log(Log log, ConfigMeta sourceMeta, ConfigMeta realMeta);
}
