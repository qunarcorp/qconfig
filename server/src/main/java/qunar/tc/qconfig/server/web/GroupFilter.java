package qunar.tc.qconfig.server.web;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.support.context.ClientInfoService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2017 2017/11/24 15:54
 */
public class GroupFilter implements Filter {

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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String group = request.getParameter(Constants.GROUP_NAME);
        if (group != null) {
            request.setAttribute(Constants.GROUP_NAME, group);
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
