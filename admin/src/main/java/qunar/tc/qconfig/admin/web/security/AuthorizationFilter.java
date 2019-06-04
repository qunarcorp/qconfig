package qunar.tc.qconfig.admin.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.support.AuthorizationControl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 21:01
 */
public class AuthorizationFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private AuthorizationControl controller;
    private UnauthorizedHandler handler;

    private static final int REDIRECT_POSITION = 9;

    private static final int HANDLER_POSITION = 8;

    @Override
    public void init(FilterConfig config) throws ServletException {

        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());

        String controllerId = config.getInitParameter("controller");
        if (controllerId == null || controllerId.isEmpty()) {
            controller = wac.getBean(AuthorizationControl.class);
        } else {
            controller = wac.getBean(controllerId, AuthorizationControl.class);
        }

        String handlerId = config.getInitParameter("handler");
        if (handlerId == null) {
            // default 403
            handler = new ErrorCodeHandler(HttpServletResponse.SC_FORBIDDEN, "");
        } else if (handlerId.startsWith("redirect:")) {
            String location = handlerId.substring(REDIRECT_POSITION);
            logger.debug("if unauthored, redirect to {}", location);
            handler = new RedirectHandler(location);
        } else if (handlerId.startsWith("handler:")) {
            String clazz = handlerId.substring(HANDLER_POSITION);
            try {
                Object obj = Class.forName(clazz).newInstance();
                if (obj instanceof UnauthorizedHandler) {
                    handler = (UnauthorizedHandler) obj;
                } else {
                    throw new IllegalArgumentException("'" + clazz + "' is not an implementation of "
                            + UnauthorizedHandler.class.getName());
                }
            } catch (Exception e) {
                logger.error("failed to initialize " + clazz, e);
            }
        } else {
            throw new RuntimeException("invalid handler config: " + handlerId);
        }
    }

    protected ServletRequest preProcessRequest(ServletRequest request) {
        return request;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (controller.isCheckRequired((HttpServletRequest)request)) {
            request = preProcessRequest(request);
            if (!controller.isAuthorized((HttpServletRequest)request)) {
                handler.handle((HttpServletResponse) response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    public interface UnauthorizedHandler {
        void handle(HttpServletResponse response) throws IOException, ServletException;
    }

    /**
     * 无权限HTTP响应处理
     */
    class ErrorCodeHandler implements UnauthorizedHandler {

        private int statusCode = -1;
        private String message = null;

        public ErrorCodeHandler(int errorCode, String message) {
            this.statusCode = errorCode;
            this.message = message;
        }

        @Override
        public void handle(HttpServletResponse response) throws IOException {
            response.sendError(statusCode, message);
        }
    }

    /**
     * 无权限跳转
     */
    class RedirectHandler implements UnauthorizedHandler {
        private String location = null;

        public RedirectHandler(String location) {
            this.location = location;
        }

        @Override
        public void handle(HttpServletResponse response) throws IOException {
            response.sendRedirect(location);
        }
    }
}
