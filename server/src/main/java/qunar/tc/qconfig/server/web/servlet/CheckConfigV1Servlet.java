package qunar.tc.qconfig.server.web.servlet;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-8
 * Time: 下午12:19
 */
public class CheckConfigV1Servlet extends AbstractCheckConfigServlet {

    private static final long serialVersionUID = -5771751244857815108L;

    @Override
    protected String getVersion() {
        return V1Version;
    }
}
