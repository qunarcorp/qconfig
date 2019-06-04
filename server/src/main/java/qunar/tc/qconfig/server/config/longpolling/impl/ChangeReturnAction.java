package qunar.tc.qconfig.server.config.longpolling.impl;

import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.support.monitor.Monitor;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 19:23
 */
public class ChangeReturnAction implements ReturnAction {

    private final String change;
    private final String type;

    public ChangeReturnAction(String change, String type) {
        this.change = change;
        this.type = type;
    }

    @Override
    public String type() {
        return "changes";
    }

    @Override
    public void act(AsyncContext context) throws Exception {
        try {
            HttpServletResponse response = (HttpServletResponse) context.getResponse();
            response.setHeader(Constants.UPDATE_TYPE, type);
            response.getWriter().write(change);
        } catch (Exception e) {
            Monitor.returnChangeFailCounter.inc();
            throw e;
        }
    }
}
