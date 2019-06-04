package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.config.cache.CacheFixedVersionConsumerService;
import qunar.tc.qconfig.server.support.AdminUtil;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.MetaIp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class FixedVersionServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(FixedVersionServlet.class);
    private ClientInfoService clientInfoService;
    private CacheFixedVersionConsumerService cacheFixedVersionConsumerService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }
        clientInfoService = context.getBean(ClientInfoService.class);
        cacheFixedVersionConsumerService = context.getBean(CacheFixedVersionConsumerService.class);
        Preconditions.checkNotNull(clientInfoService);
        Preconditions.checkNotNull(cacheFixedVersionConsumerService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!hasPermission()) {
            logger.warn("non qconfig admin machine try push, group:{}, {}:{}", clientInfoService.getGroup(), clientInfoService.getIp(), clientInfoService.getPort());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ConfigMeta meta = new ConfigMeta(req.getParameter(Constants.GROUP_NAME), req.getParameter(Constants.DATAID_NAME), req.getParameter(Constants.PROFILE_NAME));
        String ip = req.getParameter("ip");
        long version = Long.parseLong(req.getParameter(Constants.VERSION_NAME));
        cacheFixedVersionConsumerService.update(new MetaIp(meta, ip), version);
    }

    private boolean hasPermission() {
        return Objects.equals(AdminUtil.getAdminApp(), clientInfoService.getGroup());
    }

}
