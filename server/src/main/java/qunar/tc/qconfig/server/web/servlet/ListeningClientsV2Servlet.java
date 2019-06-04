package qunar.tc.qconfig.server.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Created by chenjk on 2018/8/2.
 */
public class ListeningClientsV2Servlet extends AbstractServlet {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    protected String getVersion() {
        return V2Version;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConfigMeta meta = new ConfigMeta(req.getParameter(Constants.GROUP_NAME), req.getParameter(Constants.DATAID_NAME), req.getParameter(Constants.PROFILE_NAME));
        Set<ClientData> clientDataSet = longPollingProcessService.getListeningClientsData(meta);
        resp.setContentType(ContentType.APPLICATION_JSON.toString());
        jsonMapper.writeValue(resp.getWriter(), clientDataSet);
        resp.getWriter().flush();
    }
}
