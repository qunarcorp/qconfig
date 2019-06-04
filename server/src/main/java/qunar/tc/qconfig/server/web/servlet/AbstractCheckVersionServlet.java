package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.server.domain.CheckRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by dongcao on 2018/5/29.
 */
public abstract class AbstractCheckVersionServlet extends AbstractServlet {

    private static final long serialVersionUID = -8278568383506314625L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<CheckRequest> requests;
        try {
            requests = getRequestParser().parse(req);
        } catch (IOException e) {
            logger.error("bad check request", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (requests.isEmpty()) {
            logger.error("bad check request, request is empty");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!hasPermission(requests)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        checkVersion(requests, req, resp);
    }

    protected abstract void checkVersion(List<CheckRequest> checkRequests, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException;

    protected boolean hasPermission(List<CheckRequest> requests) {
        return true;
    }

}
