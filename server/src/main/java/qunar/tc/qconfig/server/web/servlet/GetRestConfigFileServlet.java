package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Numbers;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.exception.IllegalFileTypeException;
import qunar.tc.qconfig.server.config.rest.RestFile;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.server.config.rest.RestApiResponse;
import qunar.tc.qconfig.servercommon.util.JacksonSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * rest类型文件获取
 * <p>
 * Created by chenjk on 2017/8/7.
 */
public class GetRestConfigFileServlet extends AbstractServlet implements QFileFactoryGetter {

    private static final long serialVersionUID = -1173227790471936530L;

    private static final Logger logger = LoggerFactory.getLogger(GetRestConfigFileServlet.class);

    private static final JacksonSerializer jsonMapper = JacksonSerializer.getSerializer();

    private void writeStatusResponse(HttpServletResponse resp, int statusCode, RestApiResponse restApiResponse) throws IOException {
        resp.setStatus(statusCode);
        resp.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
        resp.getWriter().write(jsonMapper.serialize(restApiResponse));
        resp.flushBuffer();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = (String) req.getAttribute(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        String profile = req.getParameter(Constants.PROFILE_NAME);
        if (Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId) || Strings.isNullOrEmpty(profile)) {
            writeBadRequest(resp);
            return;
        }

        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        Optional<Long> v = getVersion(req);
        try {
            RestFile restFile = getRestConfigService().getRestFile(meta, v, getQFileFactory());
            logger.debug("get rest file {}", restFile);
            writeFile(resp, restFile);
        } catch (IllegalFileTypeException | ConfigNotFoundException e) {
            if (e instanceof ConfigNotFoundException) {
                logger.error("未能定位到配置文件, version [{}], {}", v, meta);
            }
            writeNotFoundRequest(resp);
            return;
        }
    }

    private void writeNotFoundRequest(HttpServletResponse resp) throws IOException {
        writeStatusResponse(resp, HttpServletResponse.SC_NOT_FOUND, RestApiResponse.FILE_NOT_FOUND);
    }

    private Optional<Long> getVersion(HttpServletRequest req) {
        String strVersion = req.getParameter(Constants.VERSION_NAME);
        // 使用isNumeric来判断，字母的，乱写的，写错的version也会走这个分支，然后去请求最新的版本，
        // 但是也不知道是不是有人带上字母来请求，还是保持原来的逻辑
        if (Strings.isNullOrEmpty(strVersion) || !StringUtils.isNumeric(strVersion)) {
            return Optional.absent();
        } else {
            return Optional.of(Numbers.toLong(strVersion));
        }
    }

    private void writeFile(HttpServletResponse resp, RestFile restFile) throws IOException {
        resp.addHeader(Constants.VERSION_NAME, String.valueOf(restFile.getVersion()));
        resp.addHeader(Constants.CHECKSUM_NAME, restFile.getChecksum());
        resp.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
        resp.getWriter().write(restFile.getData());
        resp.flushBuffer();
    }

    private void writeBadRequest(HttpServletResponse resp) throws IOException {
        writeStatusResponse(resp, HttpServletResponse.SC_BAD_REQUEST, RestApiResponse.BAD_REQUEST_RESPONSE);
    }

    @Override
    protected String getVersion() {
        return V2Version;
    }
}
