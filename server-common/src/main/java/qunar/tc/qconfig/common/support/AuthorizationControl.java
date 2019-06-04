package qunar.tc.qconfig.common.support;

import javax.servlet.http.HttpServletRequest;

public interface AuthorizationControl {

    boolean isCheckRequired(HttpServletRequest request);

    boolean isAuthorized(HttpServletRequest request);
}
