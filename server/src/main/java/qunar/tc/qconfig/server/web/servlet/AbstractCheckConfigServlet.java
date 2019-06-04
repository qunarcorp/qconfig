package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutDataId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/10/28 15:36
 */
public abstract class AbstractCheckConfigServlet extends AbstractCheckVersionServlet implements QFileFactoryGetter {

    private static final long serialVersionUID = -6847122246858658101L;

    @Override
    protected void checkVersion(List<CheckRequest> checkRequests, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String profile = clientInfoService.getProfile();
        if (logger.isDebugEnabled()) {
            logger.debug("profile:{}, check request:{}", profile, checkRequests);
        }

        String group = getClientInfoService().getGroup();
        getStatisticsService().recordConfigs(new ConfigMetaWithoutDataId(group, profile), checkRequests.size());

        Collection<Changed> changedList = getWrappedConfigService().checkChangedList(getQFileFactory(), checkRequests,
                clientInfoService.getIp(), Boolean.parseBoolean(req.getHeader(Constants.NEED_PURGE)));
        doReturn(changedList, resp);

        logger.info("profile:{}, result change list {} for check request {}", profile, changedList, checkRequests);
    }

    public static String formatOutput(Collection<Changed> changedList) {
        StringBuilder builder = new StringBuilder();
        for (Changed changed : changedList) {
            builder.append(changed.getGroup()).append(",")
                    .append(changed.getDataId()).append(",")
                    .append(changed.getNewestVersion()).append(",")
                    .append(changed.getProfile()).append(Constants.LINE);
        }
        return builder.toString();
    }

    protected void doReturn(Collection<Changed> changedList, HttpServletResponse resp) throws IOException {
        if (changedList == null || changedList.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            resp.getWriter().write(formatOutput(changedList));
            resp.flushBuffer();
        }
    }
}
