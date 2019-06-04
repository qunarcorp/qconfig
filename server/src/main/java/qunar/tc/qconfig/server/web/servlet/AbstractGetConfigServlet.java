package qunar.tc.qconfig.server.web.servlet;

import com.google.common.collect.Maps;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 12:08
 */
public abstract class AbstractGetConfigServlet extends AbstractLoadConfigServlet implements QFileFactoryGetter {

    private static final long serialVersionUID = 8680368279237326566L;

    @Override
    protected Map.Entry<QFile, VersionData<ChecksumData<String>>> loadConfig(VersionData<ConfigMeta> versionMeta)
            throws ConfigNotFoundException {
        Map.Entry<QFile, ChecksumData<String>> fileConfig = getWrappedConfigService().findConfig(getQFileFactory(), versionMeta);
        VersionData<ChecksumData<String>> fileContent = new VersionData<>(versionMeta.getVersion(), fileConfig.getValue());

        return Maps.immutableEntry(fileConfig.getKey(), fileContent);
    }

}
