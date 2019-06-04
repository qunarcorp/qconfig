package qunar.tc.qconfig.server.web.servlet;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 13:49
 */
public class RecordLoadingV2Servlet extends AbstractRecordLoadingServlet {

    private static final long serialVersionUID = -1808419436188826490L;

    @Override
    protected String getVersion() {
        return V2Version;
    }
}
