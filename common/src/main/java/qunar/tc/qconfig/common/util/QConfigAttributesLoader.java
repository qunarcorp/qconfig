package qunar.tc.qconfig.common.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhenyu.nie created on 2016 2016/10/25 21:05
 */
public class QConfigAttributesLoader {

    private static final Logger logger = LoggerFactory.getLogger(QConfigAttributesLoader.class);

    private static final DefaultPropertiesLoader defaultPropertiesLoader = DefaultPropertiesLoader.getInstance();

    private static final String DEFAULT_ADDRESS_KEY = "qconfig.default.serverlist";
    private static final String DEFAULT_HTTPS_ADDRESSES_KEY = "qconfig.default.httpsserverlist";
    private static final String SERVER_URL_key = "qconfig.server.host";
    private static final String ADMIN_URL_KEY = "qconfig.admin.host";
    private static final String DEV_KEY = "qconfig.symbol.dev";
    private static final String BETA_KEY = "qconfig.symbol.beta";
    private static final String PROD_KEY = "qconfig.symbol.prod";
    private static final String RESOURCES_KEY = "qconfig.symbol.resources";
    private static final String BUILDGROUP_KEY ="qconfig.symbol.buildgroup";
    private static final String SERVERAPP_KEY = "qconfig.server.app";
    private static final String REGISTER_SELF_ON_START_KEY = "qconfig.server.register_self_on_start";

    private static final Supplier<QConfigAttributes> attrSupplier = Suppliers.memoize(new Supplier<QConfigAttributes>() {
        @Override
        public QConfigAttributes get() {
            String resources = System.getProperty(RESOURCES_KEY, defaultPropertiesLoader.getDefaultValue(RESOURCES_KEY));
            String buildGroup = System.getProperty(BUILDGROUP_KEY, defaultPropertiesLoader.getDefaultValue(BUILDGROUP_KEY));
            String defaultAddresses = System.getProperty(DEFAULT_ADDRESS_KEY, defaultPropertiesLoader.getDefaultValue(DEFAULT_ADDRESS_KEY));
            String defaultHttpsAddresses = System.getProperty(DEFAULT_HTTPS_ADDRESSES_KEY, defaultPropertiesLoader.getDefaultValue(DEFAULT_HTTPS_ADDRESSES_KEY));
            String serverUrl = System.getProperty(SERVER_URL_key, defaultPropertiesLoader.getDefaultValue(SERVER_URL_key));
            String adminUrl = System.getProperty(ADMIN_URL_KEY, defaultPropertiesLoader.getDefaultValue(ADMIN_URL_KEY));
            String dev = System.getProperty(DEV_KEY, defaultPropertiesLoader.getDefaultValue(DEV_KEY));
            String beta = System.getProperty(BETA_KEY, defaultPropertiesLoader.getDefaultValue(BETA_KEY));
            String prod = System.getProperty(PROD_KEY, defaultPropertiesLoader.getDefaultValue(PROD_KEY));
            String serverApp = System.getProperty(SERVERAPP_KEY, defaultPropertiesLoader.getDefaultValue(SERVERAPP_KEY));
            boolean registerSelfOnStart = Boolean.valueOf(System.getProperty(REGISTER_SELF_ON_START_KEY, defaultPropertiesLoader.getDefaultValue(REGISTER_SELF_ON_START_KEY)));

            logger.info("qconfig attributes, default addressed [{}], server url [{}], admin url [{}], " +
                            "dev [{}], beta [{}], prod [{}], resources [{}], build group [{}], server app [{}], register self on start[{}]",
                    defaultAddresses, serverUrl, adminUrl, dev, beta, prod, resources, buildGroup, serverApp, registerSelfOnStart);

            return new QConfigAttributes.Builder()
                    .setDefaultAddresses(defaultAddresses)
                    .setDefaultHttpsAddresses(defaultHttpsAddresses)
                    .setServerUrl(serverUrl)
                    .setAdminUrl(adminUrl)
                    .setDev(dev)
                    .setBeta(beta)
                    .setProd(prod)
                    .setResources(resources)
                    .setBuildGroup(buildGroup)
                    .setServerApp(serverApp)
                    .setRegisterSelfOnStart(registerSelfOnStart)
                    .build();
        }
    });

    public static QConfigAttributes getInstance() {
        return attrSupplier.get();
    }
}
