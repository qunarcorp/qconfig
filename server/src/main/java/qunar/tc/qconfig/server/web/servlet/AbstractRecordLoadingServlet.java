package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.Numbers;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.support.AddressUtil;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 13:46
 */
public abstract class AbstractRecordLoadingServlet extends AbstractServlet implements QFileFactoryGetter {

    private static final long serialVersionUID = -7497091493859705970L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = createRequest(req);
        logger.debug("record loading, {}", request);

        if (isIllegal(request)) {
            logger.warn("param error, {}", request);
            return;
        }

        Optional<QFile> qFile = createQFile(request);
        if (!qFile.isPresent()) {
            logger.warn("can not find qfile, {}", request);
            return;
        }

        processClientParseError(qFile.get(), request);

        try {
            logLoading(qFile.get(), request);
        } catch (Exception e) {
            logger.error("log error, {}", request, e);
        }
    }

    private void logLoading(QFile qFile, Request request) {
        qFile.log(createLog(request));
    }

    private Optional<QFile> createQFile(Request request) {
        ConfigMeta meta = new ConfigMeta(request.getGroup(), request.getDataId(), request.getProfile());
        return getQFileFactory().create(meta, getCacheConfigInfoService());
    }

    private Log createLog(Request request) {
        return new Log(request.getGroup(),
                request.getDataId(),
                request.getProfile(),
                request.getVersion(),
                request.getIp(),
                request.getPort(),
                request.getType(),
                request.getRemarks());
    }

    private boolean isIllegal(Request request) {
        return Strings.isNullOrEmpty(request.getGroup()) || Strings.isNullOrEmpty(request.getDataId()) || request.getType() == null || !AddressUtil.isLegalPortInput(request.getPort());
    }

    private long getVersion(HttpServletRequest req) {
        long version = Numbers.toLong(req.getParameter(Constants.VERSION_NAME));
        return version < 0 ? 0 : version;
    }

    private ConfigLogType getConfigLogType(String typeCode) {
        try {
            return ConfigLogType.codeOf(Integer.parseInt(typeCode));
        } catch (Exception e) {
            return null;
        }
    }

    private void processClientParseError(QFile qFile, Request request) {
        if (isProdClient() && isClientParseError(request.getType(), request.getRemarks())) {
            getMailParseErrorService().mailParseError(getClientInfoService().getGroup(), qFile.getSharedMeta(), request.getVersion());
        }
    }

    private boolean isClientParseError(ConfigLogType type, String remarks) {
        return type == ConfigLogType.PARSE_REMOTE_ERROR || (type == ConfigLogType.USE_REMOTE_FILE && !Strings.isNullOrEmpty(remarks));
    }

    private boolean isProdClient() {
        return Environment.fromEnvName(getClientInfoService().getEnv()).isProd();
    }

    private Request createRequest(HttpServletRequest req) {
        String ip = getClientInfoService().getIp();
        String group = (String) req.getAttribute(Constants.GROUP_NAME);
        String profile = clientInfoService.getProfile();
        String dataId = req.getParameter(Constants.DATAID_NAME);
        long version = getVersion(req);
        String remarks = Strings.nullToEmpty(req.getParameter(Constants.REMARKS_NAME));
        String typeCode = req.getParameter(Constants.CONFIG_LOG_TYPE_CODE);
        int port = getClientInfoService().getPort();
        ConfigLogType type = getConfigLogType(typeCode);

        return new Request(group, profile, dataId, version, ip, port, remarks, typeCode, type);
    }

    private static class Request {

        private String group;

        private String profile;

        private String dataId;

        private long version;

        private String ip;

        private int port;

        private String remarks;

        private String typeCode;

        private ConfigLogType type;

        private Request(String group, String profile, String dataId, long version, String ip, int port, String remarks, String typeCode, ConfigLogType type) {
            this.group = group;
            this.profile = profile;
            this.dataId = dataId;
            this.version = version;
            this.ip = ip;
            this.port = port;
            this.remarks = remarks;
            this.typeCode = typeCode;
            this.type = type;
        }

        public String getGroup() {
            return group;
        }

        public String getProfile() {
            return profile;
        }

        public String getDataId() {
            return dataId;
        }

        public long getVersion() {
            return version;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public String getRemarks() {
            return remarks;
        }

        public String getTypeCode() {
            return typeCode;
        }

        public ConfigLogType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "group='" + group + '\'' +
                    ", profile='" + profile + '\'' +
                    ", dataId='" + dataId + '\'' +
                    ", version=" + version +
                    ", ip='" + ip + '\'' +
                    ", port=" + port +
                    ", remarks='" + remarks + '\'' +
                    ", typeCode='" + typeCode + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
