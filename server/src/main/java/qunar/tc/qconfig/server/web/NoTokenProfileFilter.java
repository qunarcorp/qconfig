package qunar.tc.qconfig.server.web;

import com.google.common.base.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.support.context.ClientInfoService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2016 2016/11/30 17:08
 */
public class NoTokenProfileFilter implements Filter {

    private ClientInfoService clientInfoService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        clientInfoService = context.getBean(ClientInfoService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String profile = req.getHeader(Constants.PROFILE_NAME);
        if(Strings.isNullOrEmpty(profile)) {
            profile = (String)req.getParameter(Constants.PROFILE_NAME);
            if(Strings.isNullOrEmpty(profile)) {
                profile = (String)req.getAttribute(Constants.PROFILE_NAME);
            }
        }

        clientInfoService.setProfile(profile);
        clientInfoService.setNoToken(Boolean.TRUE);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
