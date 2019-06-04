package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.common.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2016 2016/11/30 15:12
 */
public class NoTokenGetConfigServlet extends GetConfigV2Servlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = (String) req.getAttribute(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        if (!getNoTokenPermissionService().hasPermission(group, dataId)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        super.doGet(req, resp);
    }
}
