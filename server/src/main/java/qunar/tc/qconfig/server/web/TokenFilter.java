package qunar.tc.qconfig.server.web;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.TypedConfig;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.server.serverself.serverinfo.ClusterType;
import qunar.tc.qconfig.server.support.app.TokenDecoder;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.servercommon.service.EnvironmentMappingService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * User: zhaohuiyu
 * Date: 5/14/14
 * Time: 2:27 PM
 */
public class TokenFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    private ClientInfoService clientInfoService;

    private EnvironmentMappingService envMappingService;

    private TokenDecoder tokenDecoder;

    private static volatile Set<String> forbiddenApps = ImmutableSet.of();
    private static volatile Set<String> whiteListApps = ImmutableSet.of();

    private static final String APP = "app";

    static {
        TypedConfig<String> typedConfig = TypedConfig.get("forbidden.app", Feature.create().setFailOnNotExists(false).build(), TypedConfig.STRING_PARSER);
        typedConfig.current();
        typedConfig.addListener(new Configuration.ConfigListener<String>() {
            @Override
            public void onLoad(String conf) {
                try {
                    ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<String>();
                    List<String> lines = CharSource.wrap(conf).readLines();
                    for (String line : lines) {
                        line = line.trim();
                        if (Strings.isNullOrEmpty(line)) {
                            continue;
                        }
                        builder.add(line);
                    }
                    forbiddenApps = builder.build();
                    logger.info("forbidden apps is [{}]", forbiddenApps);
                } catch (IOException e) {
                    logger.error("unexpected error", e);
                }
            }
        });
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                String whiteListAppStr = conf.get("whiteListApps");
                if (!Strings.isNullOrEmpty(whiteListAppStr)) {
                    whiteListApps = new HashSet<>(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(whiteListAppStr));
                }
            }
        });
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        envMappingService = context.getBean(EnvironmentMappingService.class);
        tokenDecoder = context.getBean(TokenDecoder.class);
        clientInfoService = context.getBean(ClientInfoService.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String token = request.getHeader(Constants.TOKEN_NAME);
        String profile = request.getHeader(Constants.PROFILE_NAME);
        String env = request.getHeader(Constants.ENV_NAME);

        logger.debug("client access config, profile {}, token: {}", profile, token);

        String group = tokenDecoder.decodeToken(token);

        if (group == null) {
            logger.error("decode client token failed, token: {}", token);
            Monitor.forbidAccessCounter.inc();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (forbiddenApps.contains(group)) {
            logger.debug("forbidden app access, [{}]", group);
            Monitor.forbidAppAccessCounter.inc();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        clientInfoService.setGroup(group);
        String mappedEnv = envMappingService.getMappedEnv(env);
        clientInfoService.setEnv(mappedEnv);
        String clientProfile = Strings.isNullOrEmpty(profile) ? mappedEnv + ":" : mappedEnv + ":" + profile;
        clientInfoService.setProfile(clientProfile);
        if (whiteListApps.contains(group)) {
            logger.debug("app:[{}] in white list", group);
            String reqGroup = request.getParameter(Constants.GROUP_NAME);
            String reqProfile = request.getParameter("reqProfile");
            if (!Strings.isNullOrEmpty(reqGroup) && ProfileUtil.legalProfile(reqProfile)) {
                clientInfoService.setGroup(reqGroup);
                clientInfoService.setProfile(reqProfile);
                int idx = reqProfile.indexOf(":");
                clientInfoService.setEnv(reqProfile.substring(0, idx));
            }
        }

        ((HttpServletResponse) servletResponse).setHeader("Connection", "keep-alive");
        servletResponse.setContentType(Constants.DEFAULT_CONTENT_TYPE);
        try {
            MDC.put(APP, group);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(APP);
        }
    }

    @Override
    public void destroy() {
    }
}
