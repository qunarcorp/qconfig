package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.serverself.serverinfo.EntryPointService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public class EntryPointV2Servlet extends HttpServlet {

    private EntryPointService entryPointService;

    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();

    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }

        entryPointService = context.getBean(EntryPointService.class);
        Preconditions.checkNotNull(entryPointService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result = getResult(req);
        resp.getWriter().write(result);
        resp.flushBuffer();
    }

    private String getResult(HttpServletRequest req) {
        List<String> httpEntryPoints = entryPointService.getHttpEntryPoints(req);
        List<String> httpsEntryPoints = entryPointService.getHttpsEntryPoints(req);
        return COMMA_JOINER.join(httpEntryPoints)
                + Constants.LINE
                + COMMA_JOINER.join(httpsEntryPoints);
    }
}
