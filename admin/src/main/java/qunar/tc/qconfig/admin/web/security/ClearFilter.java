package qunar.tc.qconfig.admin.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.admin.service.UserContextService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/5/23 14:46
 */
public class ClearFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(ClearFilter.class);

    private UserContextService userContext;

    @Override
    public void init(FilterConfig config) throws ServletException {
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        userContext = wac.getBean(UserContextService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (RuntimeException e) {
            logger.error("", e);
            ((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getContextPath() + "/500.do");
        } finally {
            userContext.clear();
            MDC.remove(MdcConstants.CORP);
            MDC.remove(MdcConstants.USER_ID);
            MDC.remove(MdcConstants.IP);
        }
    }

    @Override
    public void destroy() {

    }
}
