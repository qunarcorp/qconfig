package qunar.tc.qconfig.server.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2018 2018/1/29 12:30
 */
public class ConfigStatisticsServlet extends AbstractServlet {

    private static final Logger logger = LoggerFactory.getLogger(ConfigStatisticsServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("do config statistics");
        boolean success = getStatisticsService().doConfigStatistics();
        if (success) {
            resp.getWriter().println("successOf");
        } else {
            resp.getWriter().println("concurrent get");
        }
        resp.getWriter().flush();
    }

    @Override
    protected String getVersion() {
        return V2Version;
    }
}
