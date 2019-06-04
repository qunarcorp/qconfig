package qunar.tc.qconfig.admin.web.security;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.support.AuthorizationControl;
import qunar.tc.qconfig.servercommon.util.RequestUtil;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 10:03
 */
@Service("qCloudLoginAuthorizationService")
public class QCloudLoginAuthorizationService extends AbstractAuthorizationService implements AuthorizationControl {

    private final static String USER_ID_COOKIE_NAME = "qcloud_user_name";
    private final static String USER_TYPE_COOKIE_NAME = "user_type";
    private final static String ACCOUNT_COOKIE_NAME = "qcloud_tenant_id";

    @Resource
    private UserContextService userContext;

    private static final Set<String> NO_CHECK_REQUIRED_PREFIX = new HashSet<String>() {
        {
            add("/login.do");
            add("/500.do");
            add("/excel.do");
        }
    };

    @Override
    public boolean isCheckRequired(HttpServletRequest request) {
        String uri = uriOf(request);
        for (String prefix : NO_CHECK_REQUIRED_PREFIX) {
            if (uri.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAuthorized(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length == 0){
            return false;
        }

        String corp = "";
        String userId = "";
        String type = "";

        for (Cookie cookie : cookies) {
            if (USER_ID_COOKIE_NAME.equals(cookie.getName())) {
                userId = cookie.getValue();
                continue;
            }

            if (USER_TYPE_COOKIE_NAME.equals(cookie.getName())) {
                type = cookie.getValue();
                continue;
            }

            if (ACCOUNT_COOKIE_NAME.equals(cookie.getName())) {
                corp = cookie.getValue();
            }
        }

        if (isLogin(corp, userId, type)) {
            Account account = new Account(userId, type);
            userContext.setAccount(account);
            String ip = RequestUtil.getRealIP(request);
            userContext.setIp(ip);

            userContext.freshGroupInfos();

            MDC.put(MdcConstants.USER_ID, account.getUserId());
            MDC.put(MdcConstants.IP, ip);
            return true;
        }

        return false;
    }

    private boolean isLogin(String corp, String userId, String type) {
        return StringUtils.isNotBlank(corp) && StringUtils.isNotBlank(userId);
    }
}
