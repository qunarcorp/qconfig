package qunar.tc.qconfig.server.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 12:10
 */
public class GetConfigV1Servlet extends AbstractGetConfigServlet {
    private static final long serialVersionUID = 8449821438734413103L;

    @Override
    protected String getConfigProfile(HttpServletRequest req) {
        return clientInfoService.getProfile();
    }

    @Override
    protected String getVersion() {
        return V1Version;
    }
}
