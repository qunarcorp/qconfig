package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.server.config.ConfigService;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.server.config.apiversion.RequestParser;
import qunar.tc.qconfig.server.config.apiversion.VersionChooseService;
import qunar.tc.qconfig.server.config.cache.CacheService;
import qunar.tc.qconfig.server.config.longpolling.LongPollingProcessService;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.config.rest.RestConfigService;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.feature.MailParseErrorService;
import qunar.tc.qconfig.server.feature.StatisticsService;
import qunar.tc.qconfig.server.security.DifferentGroupAccessService;
import qunar.tc.qconfig.server.security.NoTokenPermissionService;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: zhaohuiyu
 * Date: 5/14/14
 * Time: 2:13 PM
 */
public abstract class AbstractServlet extends HttpServlet {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String V1Version = "v1";
    protected static final String V2Version = "v2";

    private static final long serialVersionUID = -600699834754594710L;

    protected ConfigService wrappedConfigService;

    protected ConfigStore configStore;

    protected ClientInfoService clientInfoService;

    protected ConfigInfoService cacheConfigInfoService;

    protected VersionChooseService versionChooseService;

    protected DifferentGroupAccessService groupAccessService;

    protected MailParseErrorService mailParseErrorService;

    protected NoTokenPermissionService noTokenPermissionService;

    protected LongPollingProcessService longPollingProcessService;

    protected StatisticsService statisticsService;

    protected CacheService cacheService;

    protected RestConfigService restConfigService;

    protected static volatile Set<String> needDoPluginAppids = Sets.newHashSet();

    private static final Splitter SPLITTER = Splitter.on(",").trimResults();

    private static final MapConfig config;

    static {
        config = MapConfig.get("config.properties", Feature.create().setFailOnNotExists(false).build());
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                needDoPluginAppids.addAll(SPLITTER.splitToList(conf.get("server.need.doplugin.appids")));
            }
        });
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }

        cacheConfigInfoService = (ConfigInfoService) context.getBean("cacheConfigInfoService");
        wrappedConfigService = context.getBean(ConfigService.class);
        clientInfoService = context.getBean(ClientInfoService.class);
        mailParseErrorService = context.getBean(MailParseErrorService.class);
        noTokenPermissionService = context.getBean(NoTokenPermissionService.class);
        longPollingProcessService = context.getBean(LongPollingProcessService.class);
        configStore = context.getBean(ConfigStore.class);
        versionChooseService = (VersionChooseService) context.getBean(getVersion());
        cacheService = context.getBean(CacheService.class);
        statisticsService = context.getBean(StatisticsService.class);
        this.groupAccessService = context.getBean(DifferentGroupAccessService.class);
        restConfigService = context.getBean(RestConfigService.class);
    }

    protected abstract String getVersion();

    public ConfigInfoService getCacheConfigInfoService() {
        return cacheConfigInfoService;
    }

    public ConfigService getWrappedConfigService() {
        return wrappedConfigService;
    }

    public ClientInfoService getClientInfoService() {
        return clientInfoService;
    }

    public QFileFactory getQFileFactory() {
        return versionChooseService.getQFileFactory();
    }

    public RequestParser getRequestParser() {
        return versionChooseService.getRequestParser();
    }

    public DifferentGroupAccessService getGroupAccessService() {
        return groupAccessService;
    }

    public MailParseErrorService getMailParseErrorService() {
        return mailParseErrorService;
    }

    public NoTokenPermissionService getNoTokenPermissionService() {
        return noTokenPermissionService;
    }

    public LongPollingProcessService getLongPollingProcessService() {
        return longPollingProcessService;
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public RestConfigService getRestConfigService() {
        return restConfigService;
    }

    protected Set<String> acceptEncodingSet(String encodingStr) {
        if (Strings.isNullOrEmpty(encodingStr)) {
            return null;
        }
        List<String> encodingSet = Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(encodingStr);
        return ImmutableSet.copyOf(encodingSet);
    }

    public ConfigStore getConfigStore() {
        return configStore;
    }


}
