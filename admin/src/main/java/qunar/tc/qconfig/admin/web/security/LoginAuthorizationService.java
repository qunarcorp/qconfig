package qunar.tc.qconfig.admin.web.security;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.support.AuthorizationControl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 10:03
 */
@Service("loginAuthorizationService")
public class LoginAuthorizationService extends AbstractAuthorizationService implements AuthorizationControl {

    @Resource
    private UserContextService userContext;

    private static final Set<String> NO_CHECK_REQUIRED_PREFIX = new HashSet<String>() {
        {
            add("/qconfig");
            add("/login.do");
            add("/500.do");
        }
    };

    @Override
    public boolean isCheckRequired(HttpServletRequest request) {
        return true;
    }

    @Override
    public boolean isAuthorized(HttpServletRequest req) {
        return true;
    }
}
