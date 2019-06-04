package qunar.tc.qconfig.server.config.longpolling.impl;

import qunar.tc.qconfig.servercommon.bean.IpAndPort;

/**
 * @author zhenyu.nie created on 2018 2018/4/17 15:59
 */
interface RateLimitService {

    boolean tryAcquire(IpAndPort address);
}
