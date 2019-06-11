package qunar.tc.qconfig.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.servercommon.util.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2018 2018/4/18 14:12
 */
public class EntrypointFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(EntrypointFilter.class);

    private ClientInfoService clientInfoService;

    private static final Logger entryLgger = LoggerFactory.getLogger("qunar.tc.qconfig.entryPoint");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        this.clientInfoService = context.getBean(ClientInfoService.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        clientInfoService.setGroup(Constants.GROUP_NAME);
        clientInfoService.setEnv(request.getHeader(Constants.ENV_NAME));
        clientInfoService.setProfile(request.getHeader(Constants.PROFILE_NAME));
        clientInfoService.setIp(RequestUtil.getRealIP(request));


        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            clientInfoService.clear();
        }
    }


    @Override
    public void destroy() {

    }


}
