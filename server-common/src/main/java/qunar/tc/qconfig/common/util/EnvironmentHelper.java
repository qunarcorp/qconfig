package qunar.tc.qconfig.common.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class EnvironmentHelper {
    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    static final Set<String> RESOURCES_SET = getEnvNamesOfType(EnvType.RESOURCES);
    static final Set<String> PROD_SET = getEnvNamesOfType(EnvType.PROD);
    static final Set<String> BETA_SET = getEnvNamesOfType(EnvType.BETA);
    static final Set<String> DEV_SET = getEnvNamesOfType(EnvType.DEV);

    private final static Map<String, EnvType> NAME_MAPPING = new HashMap<String, EnvType>();

    static {
        for (String envName : RESOURCES_SET) {
            NAME_MAPPING.put(envName, EnvType.RESOURCES);
        }
        for (String envName : PROD_SET) {
            NAME_MAPPING.put(envName, EnvType.PROD);
        }
        for (String envName : BETA_SET) {
            NAME_MAPPING.put(envName, EnvType.BETA);
        }
        for (String envName : DEV_SET) {
            NAME_MAPPING.put(envName, EnvType.DEV);
        }

    }

    public static EnvType fromName(String envName) {
        if (NAME_MAPPING.containsKey(envName)) {
            return NAME_MAPPING.get(envName);
        } else {
            return EnvType.OTHER;
        }
    }

    private static Set<String> getEnvNamesOfType(EnvType envType) {
        String envs = QConfigAttributesLoader.getInstance().getSymbol(envType.getName());
        return ImmutableSet.copyOf(SPLITTER.splitToList(envs));
    }

    abstract boolean isResources(Environment env);

    abstract boolean isProd(Environment env);

    abstract boolean isBeta(Environment env);

    abstract boolean isDev(Environment env);

    abstract EnvType getEnvType(Environment env);

    public enum EnvType {

        RESOURCES, PROD, BETA, DEV, OTHER;

        public String getName() {
            return name().toLowerCase();
        }
    }
}
