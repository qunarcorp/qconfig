package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 12:15
 */
public abstract class AbstractForceLoadServlet extends AbstractLoadConfigServlet implements QFileFactoryGetter {

    private static final long serialVersionUID = -5377603378590391692L;

    @Override
    protected String getConfigProfile(HttpServletRequest req) {
        return clientInfoService.getProfile();
    }

    @Override
    protected Map.Entry<QFile, VersionData<ChecksumData<String>>> loadConfig(VersionData<ConfigMeta> versionMeta)
            throws ConfigNotFoundException {
        return getWrappedConfigService().forceLoad(getQFileFactory(), clientInfoService.getIp(), versionMeta);
    }

}
