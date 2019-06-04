package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.server.serverself.serverinfo.EntryPointService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: zhaohuiyu
 * Date: 5/19/14
 * Time: 10:38 AM
 */
public class EntryPointServlet extends HttpServlet {
    private static final long serialVersionUID = 8273845070876584933L;

    private static final Joiner COMMA_JOINER = Joiner.on(',').skipNulls();

    private EntryPointService entryPointService;

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
        resp.getWriter().write(COMMA_JOINER.join(entryPointService.getHttpEntryPoints(req)));
        resp.flushBuffer();
    }
}
