package qunar.tc.qconfig.server.serverself.eureka;

import com.netflix.appinfo.*;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.converters.JsonXStream;
import com.netflix.discovery.converters.XmlXStream;
import com.netflix.eureka.*;
import com.netflix.eureka.cluster.PeerEurekaNodes;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.DefaultServerCodecs;
import com.netflix.eureka.resources.ServerCodecs;
import com.netflix.eureka.util.EurekaMonitors;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.application.ServerManager;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.util.Date;

/**
 * @author zhenyu.nie created on 2017 2017/8/9 20:08
 */
@Service
public class EurekaStore implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EurekaStore.class);

    private static final String TEST = "test";

    private static final String ARCHAIUS_DEPLOYMENT_ENVIRONMENT = "archaius.deployment.environment";

    private static final String EUREKA_ENVIRONMENT = "eureka.environment";

    private static final String DEFAULT = "default";

    private static final String ARCHAIUS_DEPLOYMENT_DATACENTER = "archaius.deployment.datacenter";

    private static final String EUREKA_DATACENTER = "eureka.datacenter";

    private volatile EurekaServerContext serverContext;

    private WebApplicationContext context;

    private EurekaClient eurekaClient;

    @Resource
    private EurekaClientConfig eurekaClientConfig;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() != null) {
            return;
        }

        logger.info("init eureka store");
        try {
            context = (WebApplicationContext) applicationContext;

            initEurekaEnvironment();
            initEurekaServerContext();

            ServletContext sc = context.getServletContext();
            sc.setAttribute(EurekaServerContext.class.getName(), serverContext);
        } catch (Throwable e) {
            logger.error("Cannot bootstrap eureka server :", e);
            throw new RuntimeException("Cannot bootstrap eureka server :", e);
        }
    }


    /**
     * Users can override to initialize the environment themselves.
     */
    protected void initEurekaEnvironment() throws Exception {
        logger.info("Setting the eureka configuration..");

        String dataCenter = ConfigurationManager.getConfigInstance().getString(EUREKA_DATACENTER);
        if (dataCenter == null) {
            logger.info("Eureka data center value eureka.datacenter is not set, defaulting to default");
            ConfigurationManager.getConfigInstance().setProperty(ARCHAIUS_DEPLOYMENT_DATACENTER, DEFAULT);
        } else {
            ConfigurationManager.getConfigInstance().setProperty(ARCHAIUS_DEPLOYMENT_DATACENTER, dataCenter);
        }
        String environment = ConfigurationManager.getConfigInstance().getString(EUREKA_ENVIRONMENT);
        if (environment == null) {
            ConfigurationManager.getConfigInstance().setProperty(ARCHAIUS_DEPLOYMENT_ENVIRONMENT, TEST);
            logger.info("Eureka environment value eureka.environment is not set, defaulting to test");
        }
    }

    /**
     * init hook for server context. Override for custom logic.
     */
    protected void initEurekaServerContext() throws Exception {
        EurekaServerConfig eurekaServerConfig = new DefaultEurekaServerConfig();

        // For backward compatibility
        JsonXStream.getInstance().registerConverter(new V1AwareInstanceInfoConverter(), XStream.PRIORITY_VERY_HIGH);
        XmlXStream.getInstance().registerConverter(new V1AwareInstanceInfoConverter(), XStream.PRIORITY_VERY_HIGH);

        logger.info("Initializing the eureka client...");
        logger.info(eurekaServerConfig.getJsonCodecName());
        ServerCodecs serverCodecs = new DefaultServerCodecs(eurekaServerConfig);

        ApplicationInfoManager applicationInfoManager;

        if (eurekaClient == null) {
            EurekaInstanceConfig instanceConfig = new CustomDataCenterInstanceConfig();

            applicationInfoManager = new ApplicationInfoManager(
                    instanceConfig, new InstanceInfoGetter(instanceConfig).get());

//            EurekaClientConfig eurekaClientConfig = new DefaultEurekaClientConfig();
            eurekaClient = new DiscoveryClient(applicationInfoManager, eurekaClientConfig);
        } else {
            applicationInfoManager = eurekaClient.getApplicationInfoManager();
        }

        PeerAwareInstanceRegistry registry;

        registry = new PeerAwareInstanceRegistryImpl(
                eurekaServerConfig,
                eurekaClient.getEurekaClientConfig(),
                serverCodecs,
                eurekaClient);


        PeerEurekaNodes peerEurekaNodes = getPeerEurekaNodes(
                registry,
                eurekaServerConfig,
                eurekaClient.getEurekaClientConfig(),
                serverCodecs,
                applicationInfoManager
        );

        serverContext = new DefaultEurekaServerContext(
                eurekaServerConfig,
                serverCodecs,
                registry,
                peerEurekaNodes,
                applicationInfoManager
        );

        EurekaServerContextHolder.initialize(serverContext);

        serverContext.initialize();
        logger.info("Initialized server context");

        eurekaClient.registerHealthCheck(new HealthCheckHandler() {
            @Override
            public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus currentStatus) {
                //这里直接根据ServerManager状态返回是否上线，请在这里进行自定义
                boolean online = ServerManager.getInstance().healthCheck();
                return online ? InstanceInfo.InstanceStatus.UP : InstanceInfo.InstanceStatus.DOWN;
            }
        });

        // Copy registry from neighboring eureka node
        int registryCount = registry.syncUp();
        registry.openForTraffic(applicationInfoManager, registryCount);
        // Register all monitoring statistics.
        EurekaMonitors.registerAllStats();

    }

    protected PeerEurekaNodes getPeerEurekaNodes(PeerAwareInstanceRegistry registry, EurekaServerConfig eurekaServerConfig, EurekaClientConfig eurekaClientConfig, ServerCodecs serverCodecs, ApplicationInfoManager applicationInfoManager) {
        return new PeerEurekaNodes(
                registry,
                eurekaServerConfig,
                eurekaClientConfig,
                serverCodecs,
                applicationInfoManager
        );
    }

    /**
     * Handles Eureka cleanup, including shutting down all monitors and yielding all EIPs.
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @PreDestroy
    public void destroy() {
        try {
            logger.info("{} Shutting down Eureka Server..", new Date().toString());
            ServletContext sc = context.getServletContext();
            sc.removeAttribute(EurekaServerContext.class.getName());

            DiscoveryManager.getInstance().shutdownComponent();
            destroyEurekaServerContext();
            destroyEurekaEnvironment();

        } catch (Throwable e) {
            logger.error("Error shutting down eureka", e);
        }
        logger.info("{} Eureka Service is now shutdown...", new Date().toString());
    }

    /**
     * Server context shutdown hook. Override for custom logic
     */
    protected void destroyEurekaServerContext() throws Exception {
        EurekaMonitors.shutdown();

        if (serverContext != null) {
            serverContext.shutdown();
        }
    }

    /**
     * Users can override to clean up the environment themselves.
     */
    protected void destroyEurekaEnvironment() throws Exception {

    }
}
