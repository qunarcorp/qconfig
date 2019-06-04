package qunar.tc.qconfig.common.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/10/25 21:00
 */
public class QConfigAttributes {

    private final String[] defaultAddresses;

    private String[] defaultHttpsAddresses;

    private final String serverUrl;

    private final String adminUrl;

    private final String dev;

    private final String beta;

    private final String prod;

    private final String resources;

    private final String buildGroup;

    private final Map<String, String> symbols;

    private final String serverApp;

    private final Boolean registerSelfOnStart;

    static class Builder {

        private String defaultAddresses;

        private String serverUrl;

        private String adminUrl;

        private String dev;

        private String beta;

        private String prod;

        private String resources;

        private String buildGroup;

        private String serverApp;

        private Boolean registerSelfOnStart;

        private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
        private String defaultHttpsAddresses;

        public QConfigAttributes build() {
            serverUrl = System.getProperty("qconfig.server", serverUrl);
            adminUrl = System.getProperty("qconfig.admin", adminUrl);

            Preconditions.checkArgument(!Strings.isNullOrEmpty(defaultAddresses), "default addresses can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(serverUrl), "serverUrl can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(adminUrl), "adminUrl can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(dev), "dev symbol can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(beta), "beta symbol can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(prod), "prod symbol can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(resources), "resources symbol can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(buildGroup), "build group symbol can not be empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(serverApp), "server app can not be empty");
            Preconditions.checkNotNull(registerSelfOnStart, "Boolean registerSelfOnStart can not ne null");
            List<String> addressList = COMMA_SPLITTER.splitToList(defaultAddresses);
            Preconditions.checkArgument(!addressList.isEmpty(), "default addresses can not be empty");
            String[] addresses = addressList.toArray(new String[]{});

            List<String> httpsAddressList = COMMA_SPLITTER.splitToList(defaultHttpsAddresses);
            String[] httpsAddresses = httpsAddressList.toArray(new String[]{});

            return new QConfigAttributes(addresses, httpsAddresses, serverUrl, adminUrl, dev, beta, prod, resources, buildGroup, serverApp, registerSelfOnStart);
        }



        public Builder setDefaultAddresses(String defaultAddresses) {
            this.defaultAddresses = defaultAddresses;
            return this;
        }

        public Builder setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder setAdminUrl(String adminUrl) {
            this.adminUrl = adminUrl;
            return this;
        }

        public Builder setDev(String dev) {
            this.dev = dev;
            return this;
        }

        public Builder setBeta(String beta) {
            this.beta = beta;
            return this;
        }

        public Builder setProd(String prod) {
            this.prod = prod;
            return this;
        }

        public Builder setResources(String resources) {
            this.resources = resources;
            return this;
        }

        public Builder setBuildGroup(String buildGroup) {
            this.buildGroup = buildGroup;
            return this;
        }

        public Builder setServerApp(String serverApp) {
            this.serverApp = serverApp;
            return this;
        }

        public Builder setRegisterSelfOnStart(Boolean registerSelfOnStart) {
            this.registerSelfOnStart = registerSelfOnStart;
            return this;
        }

        public Builder setDefaultHttpsAddresses(String defaultHttpsAddresses) {
            this.defaultHttpsAddresses = defaultHttpsAddresses;
            return this;
        }
    }

    private QConfigAttributes(String[] defaultAddresses, String[] defaultHttpsAddresses, String serverUrl, String adminUrl,
                              String dev, String beta, String prod, String resources, String buildGroup,
                              String serverApp, Boolean registerSelfOnStart) {
        this.defaultAddresses = defaultAddresses;
        this.defaultHttpsAddresses = defaultHttpsAddresses;
        this.serverUrl = serverUrl;
        this.adminUrl = adminUrl;
        this.dev = dev;
        this.beta = beta;
        this.prod = prod;
        this.resources = resources;
        this.buildGroup = buildGroup;
        this.serverApp = serverApp;
        this.registerSelfOnStart = registerSelfOnStart;

        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        builder.put("dev", dev);
        builder.put("beta", beta);
        builder.put("prod", prod);
        builder.put("resources", resources);
        this.symbols = builder.build();
    }

    public String[] getDefaultAddresses() {
        return defaultAddresses;
    }

    public String[] getDefaultHttpsAddresses() {
        return this.defaultHttpsAddresses;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public String getDev() {
        return dev;
    }

    public String getBeta() {
        return beta;
    }

    public String getProd() {
        return prod;
    }

    public String getResources() {
        return resources;
    }

    public String getSymbol(String key) {
        return symbols.get(key);
    }

    public String getBuildGroup() {
        return buildGroup;
    }

    public String getServerApp() {
        return serverApp;
    }

    public Boolean getRegisterSelfOnStart() {
        return registerSelfOnStart;
    }

    @Override
    public String toString() {
        return "QConfigAttributes{" +
                ", defaultAddresses=" + Arrays.toString(defaultAddresses) +
                ", serverUrl='" + serverUrl + '\'' +
                ", adminUrl='" + adminUrl + '\'' +
                ", dev='" + dev + '\'' +
                ", beta='" + beta + '\'' +
                ", prod='" + prod + '\'' +
                ", resources='" + resources + '\'' +
                ", symbols=" + symbols +
                ", buildGroup='" + buildGroup + '\'' +
                ", serverApp='" + serverApp + '\'' +
                ", registerSelfOnStart='" + registerSelfOnStart + '\'' +
                '}';
    }
}
