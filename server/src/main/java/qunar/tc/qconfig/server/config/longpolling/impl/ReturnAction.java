package qunar.tc.qconfig.server.config.longpolling.impl;

import javax.servlet.AsyncContext;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 19:35
 */
public interface ReturnAction {

    String type();

    void act(AsyncContext context) throws Exception;
}
