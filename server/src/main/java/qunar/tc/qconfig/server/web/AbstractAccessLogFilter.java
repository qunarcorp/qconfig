package qunar.tc.qconfig.server.web;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.Numbers;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.security.DifferentGroupAccessService;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.server.support.AddressUtil;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.server.web.servlet.QFileFactoryGetter;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 13:42
 */
public abstract class AbstractAccessLogFilter implements Filter, QFileFactoryGetter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ClientInfoService clientInfoService;

    private ConfigInfoService cacheConfigInfoService;

    private DifferentGroupAccessService groupAccessService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        this.clientInfoService = context.getBean(ClientInfoService.class);
        this.cacheConfigInfoService = (ConfigInfoService) context.getBean("cacheConfigInfoService");
        this.groupAccessService = context.getBean(DifferentGroupAccessService.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String profile = clientInfoService.getProfile();
        String group = (String) req.getAttribute(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        Long version = Numbers.toLong(req.getParameter(Constants.VERSION_NAME));
        int port = clientInfoService.getPort();

        if (Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId) || !AddressUtil.isLegalPortInput(port)) {
            logger.warn("param error, group is {}, profile is {}, dataId is {}, version is {}", group, profile, dataId, version);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
        recordAccessLog(group, dataId, profile, Numbers.toLong(resp.getHeader(Constants.VERSION_NAME), Constants.NO_FILE_VERSION),
                clientInfoService.getIp(), port, resp.getStatus());
    }

    private void recordAccessLog(String group, String dataId, String profile, Long version, String ip, int port, int statusCode) {
        version = version == -1 ? 0 : version;
        Log log = new Log(group, dataId, profile, version, ip, port, generateErrorInfo(statusCode));
        log.setType(statusCode == HttpServletResponse.SC_OK ? ConfigLogType.PULL_SUCCESS : ConfigLogType.PULL_ERROR);
        try {
            Optional<QFile> qFile = getQFileFactory().create(new ConfigMeta(group, dataId, profile), cacheConfigInfoService);
            if (!qFile.isPresent()) {
                logger.warn("can not find qfile with {}", log);
            } else {
                qFile.get().log(log);
            }
        } catch (Exception e) {
            logger.error("save access log error, log: {}", log, e);
        }
    }

    private String generateErrorInfo(int statusCode) {
        if (statusCode == HttpServletResponse.SC_OK) {
            return "";
        } else if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
            return "未能找到配置文件";
        } else if (statusCode == HttpServletResponse.SC_FORBIDDEN) {
            return "无访问权限";
        } else if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            return "系统异常";
        } else {
            return "返回状态码为 " + statusCode;
        }
    }

    @Override
    public void destroy() {

    }
}
