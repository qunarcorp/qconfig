package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2018 2018/5/14 17:13
 */
public class ListeningClientsServlet extends AbstractServlet {

    @Override
    protected String getVersion() {
        return V2Version;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConfigMeta meta = new ConfigMeta(req.getParameter(Constants.GROUP_NAME), req.getParameter(Constants.DATAID_NAME), req.getParameter(Constants.PROFILE_NAME));
        Set<String> ips = longPollingProcessService.getListeningClients(meta);
        PrintWriter writer = resp.getWriter();
        for (String ip : ips) {
            writer.println(ip);
        }
        writer.flush();
    }
}
