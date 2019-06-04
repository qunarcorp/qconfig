package qunar.tc.qconfig.server.feature;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2016 2016/9/21 16:05
 */
public interface MailParseErrorService {

    void mailParseError(String group, ConfigMeta meta, long version);
}
