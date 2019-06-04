package qunar.tc.qconfig.admin.web.security;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.support.AuthorizationControl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author zhenyu.nie created on 2016 2016/6/1 12:11
 */
@Service("adminAuthorizationService")
public class AdminAuthorizationService extends AbstractAuthorizationService implements AuthorizationControl {

    @Resource
    private UserContextService userContextService;

    @Override
    public boolean isCheckRequired(HttpServletRequest httpServletRequest) {
        return true;
    }

    @Override
    public boolean isAuthorized(HttpServletRequest httpServletRequest) {
        return userContextService.isAdmin();
    }
}
