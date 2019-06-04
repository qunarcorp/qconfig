package qunar.tc.qconfig.server.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.server.domain.CheckRequest;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/3/27 19:30
 */
public class LongPollingCheckServlet extends AbstractCheckVersionServlet {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingCheckServlet.class);

    @Override
    protected String getVersion() {
        return V2Version;
    }

    @Override
    protected void checkVersion(List<CheckRequest> checkRequests, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("profile:{}, long polling check request:{}", clientInfoService.getProfile(), checkRequests);
        }

        try {
            AsyncContext context = req.startAsync();
            getLongPollingProcessService().process(context, checkRequests);
        } catch (Throwable e) {
            // never come here !!!
            logger.error("服务异常", e);
        }
    }
}
