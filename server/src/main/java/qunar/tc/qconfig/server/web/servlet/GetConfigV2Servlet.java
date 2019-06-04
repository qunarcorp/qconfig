package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.common.util.Constants;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 12:11
 */
public class GetConfigV2Servlet extends AbstractGetConfigServlet {

    private static final long serialVersionUID = -5288987727928141993L;

    @Override
    protected String getConfigProfile(HttpServletRequest req) {
        return req.getParameter(Constants.LOAD_PROFILE_NAME);
    }

    @Override
    protected String getVersion() {
        return V2Version;
    }
}
