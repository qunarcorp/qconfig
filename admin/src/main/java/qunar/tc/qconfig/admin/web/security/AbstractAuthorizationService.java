package qunar.tc.qconfig.admin.web.security;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 12:37
 */
public abstract class AbstractAuthorizationService {

    protected String uriOf(HttpServletRequest request) {
        return request.getRequestURI().replaceFirst(request.getContextPath(), "");
    }
}
