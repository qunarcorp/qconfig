package qunar.tc.qconfig.server.web.servlet;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 12:18
 */
public class ForceLoadV2Servlet extends AbstractForceLoadServlet {
    private static final long serialVersionUID = 4284424904318498646L;

    @Override
    protected String getVersion() {
        return V2Version;
    }
}
