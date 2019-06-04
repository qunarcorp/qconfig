package qunar.tc.qconfig.admin.web.api;

import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.web.security.Account;
import qunar.tc.qconfig.admin.web.security.MdcConstants;
import qunar.tc.qconfig.servercommon.util.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2015 2015/4/21 1:53
 */
// todo: upload暂时不做
public class UploadFilter implements Filter {

    private UserContextService userContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        userContext = context.getBean(UserContextService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        try {
            String ip = RequestUtil.getRealIP(req);
            String rtxId = AdminConstants.CLIENT_UPLOAD_USERNAME;
            userContext.setIp(ip);
            userContext.setAccount(new Account(rtxId));
            MDC.put(MdcConstants.USER_ID, rtxId);
            MDC.put(MdcConstants.IP, ip);

            chain.doFilter(request, response);
        } catch (ServletException e) {
            if (e.getCause() instanceof MaxUploadSizeExceededException) {
                request.getRequestDispatcher("/api/config/uploadError.do").forward(request, response);
            } else {
                throw e;
            }
        } finally {
            userContext.clear();
            MDC.remove(MdcConstants.USER_ID);
            MDC.remove(MdcConstants.IP);
        }
    }

    @Override
    public void destroy() {

    }
}
