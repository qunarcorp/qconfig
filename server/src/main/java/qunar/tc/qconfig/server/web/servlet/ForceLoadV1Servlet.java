package qunar.tc.qconfig.server.web.servlet;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 12:17
 */
public class ForceLoadV1Servlet extends AbstractForceLoadServlet {
    private static final long serialVersionUID = 4291294666859848420L;

    @Override
    protected String getVersion() {
        return V1Version;
    }


}
