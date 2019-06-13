package qunar.tc.qconfig.server.web.servlet;

import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Numbers;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.exception.AccessForbiddenException;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by dongcao on 2018/5/29.
 */
public abstract class AbstractLoadConfigServlet extends AbstractServlet {

    private static final long serialVersionUID = 5757510001015440452L;

    private static final String SALT = "delicious";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = (String) req.getAttribute(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        Long version = Numbers.toLong(req.getParameter(Constants.VERSION_NAME));
        String profile = getConfigProfile(req);
        ConfigMeta queryMeta = new ConfigMeta(group, dataId, profile);
        VersionData<ConfigMeta> versionData = VersionData.of(version, queryMeta);
        if (logger.isDebugEnabled()) {
            logger.debug("get config, {}", versionData);
        }

        try {

            Map.Entry<QFile, VersionData<ChecksumData<String>>> fileConfig = loadConfig(versionData);
            QFile qFile = fileConfig.getKey();
            VersionData<ChecksumData<String>> fileContent = fileConfig.getValue();
            String resultVersion = String.valueOf(fileContent.getVersion());
            ChecksumData<String> content = fileContent.getData();
            // checkAccessPermission放在loadConfig后面，避免不存在的文件返回403。如果文件不存在，返回404，文件存在且没有权限才返403
            groupAccessService.checkAccessPermission(clientInfoService.getGroup(), group, dataId);

            if (logger.isDebugEnabled()) {
                logger.debug("load config, {}, shared meta={}, real meta={}, checksum={}, resultVersion={}",
                        versionData, qFile.getSharedMeta(), qFile.getRealMeta(), content.getCheckSum(), resultVersion);
            }

            resp.setHeader(Constants.PROFILE_NAME, qFile.getSharedMeta().getProfile());
            resp.setHeader(Constants.VERSION_NAME, resultVersion);
            resp.setStatus(HttpServletResponse.SC_OK);

            resp.setHeader(Constants.CHECKSUM_NAME, content.getCheckSum());
            resp.getWriter().write(content.getData());
        } catch (ConfigNotFoundException e) {
            Monitor.notFoundConfigFileCounterInc(group);
            logger.warn("未能定位到配置文件, {}. {}", versionData, e);
            resp.setHeader(Constants.VERSION_NAME, String.valueOf(version));
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (AccessForbiddenException e) {
            logger.warn("forbid unpublic diff group access, client group [{}], group [{}], dataId [{}]", clientInfoService.getGroup(), group, dataId);
            Monitor.forbidUnpulbicAccessCounter.inc();
            resp.addHeader(Constants.FORBIDDEN_FILE, group + ":" + dataId);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Throwable e) {
            logger.error("服务器内部异常, {}. {}" + versionData, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            resp.flushBuffer();
        }
    }

    protected abstract String getConfigProfile(HttpServletRequest req);

    protected abstract Map.Entry<QFile, VersionData<ChecksumData<String>>> loadConfig(VersionData<ConfigMeta> versionMeta)
            throws ConfigNotFoundException;

}
