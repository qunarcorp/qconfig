package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.server.domain.CheckRequest;

import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/11/30 15:07
 */
public class NoTokenCheckConfigServlet extends CheckConfigV2Servlet {

    @Override
    protected boolean hasPermission(List<CheckRequest> requests) {
        for (CheckRequest request : requests) {
            if (!getNoTokenPermissionService().hasPermission(request.getGroup(), request.getDataId())) {
                return false;
            }
        }
        return true;
    }
}
