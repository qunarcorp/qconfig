package qunar.tc.qconfig.server.web;

import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Numbers;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.servercommon.util.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/7/11 11:57
 */
public class ClientAddressFilter implements Filter {

    private static final String IP ="ip";

    private ClientInfoService clientInfoService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        this.clientInfoService = context.getBean(ClientInfoService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String ip = RequestUtil.getRealIP(req);
        int port = Numbers.toInt(req.getHeader(Constants.PORT));

        try {
            clientInfoService.setIp(ip);
            clientInfoService.setPort(port);
            MDC.put(IP, ip);
            MDC.put(Constants.PORT, String.valueOf(port));

            filterChain.doFilter(request, servletResponse);
        } finally {
            clientInfoService.clear();
            MDC.remove(IP);
            MDC.remove(Constants.PORT);
        }
    }

    @Override
    public void destroy() {

    }
}
