package qunar.tc.qconfig.server.web.servlet;

/**
 * @author zhenyu.nie created on 2014 2014/6/10 12:09
 */
public class RecordLoadingV1Servlet extends AbstractRecordLoadingServlet {

    private static final long serialVersionUID = -144380655283946420L;

    @Override
    protected String getVersion() {
        return V1Version;
    }
}
