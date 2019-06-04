package qunar.tc.qconfig.server.web.servlet;

/**
 * @author zhenyu.nie created on 2014 2014/10/28 15:09
 *
 * 允许文件重名后的check servlet，与之前的不同在于使用client端主动传过来的文件profile，
 */
public class CheckConfigV2Servlet extends AbstractCheckConfigServlet {

    private static final long serialVersionUID = 6440559988786160823L;

    @Override
    protected String getVersion() {
        return V2Version;
    }
}
