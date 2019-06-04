package qunar.tc.qconfig.client.impl;

import qunar.tc.qconfig.common.util.ConfigLogType;

/**
 * @author zhenyu.nie created on 2014 2014/6/10 11:10
 */
interface ConfigLogger {

    void log(ConfigLogType type, Meta meta, long version);

    void log(ConfigLogType type, Meta meta, long version, String message);

    void log(ConfigLogType type, Meta meta, long version, Throwable e);
}
