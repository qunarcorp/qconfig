package qunar.tc.qconfig.servercommon.service;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.common.util.Environment;
import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class EnvironmentMappingService {

    private final Logger logger = LoggerFactory.getLogger(EnvironmentMappingService.class);

    private static final String ENV_MAPPING_FILE = "env-mapping.properties";

    private static final String DEFAULT_ENV_KEY = "_default";

    private volatile static Optional<String> DEFAULT_ENV = Optional.absent();

    private volatile ImmutableMap<String, String> envMapping = ImmutableMap.of();

    @PostConstruct
    public void init() {
        MapConfig mapConfig = MapConfig.get(ENV_MAPPING_FILE, Feature.create().setFailOnNotExists(false).build());
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {

                final Map<String, String> mapping = Maps.newHashMap();
                for (Map.Entry<String, String> entry : conf.entrySet()) {
                    final String key = entry.getKey();
                    final String value = entry.getValue();
                    mapping.put(formatEnv(key), formatEnv(value));
                }
                envMapping = ImmutableMap.copyOf(mapping);
                DEFAULT_ENV = Optional.fromNullable(Strings.emptyToNull(envMapping.get(DEFAULT_ENV_KEY)));
                logger.info("env mapping updated. mapping: {}", envMapping);
            }
        });

    }

    public String getMappedEnv(String env) {
        String formattedEnv = formatEnv(env);
        if (envMapping.containsKey(formattedEnv)) {
            return envMapping.get(formattedEnv);
        } else {
            return DEFAULT_ENV.isPresent() ? DEFAULT_ENV.get() : env;
        }
    }

    public String getMappedProfile(String originProfile) {
        Environment environment = Environment.fromProfile(originProfile);
        String originEnv = environment.env();
        String buildGroup = environment.subEnv();
        String mappedEnv = getMappedEnv(originEnv);
        return mappedEnv + ":" + buildGroup;
    }

    private String formatEnv(String env) {
        return env == null ? "" : env.trim().toLowerCase();
    }


    public ImmutableMap<String, String> getEnvMapping() {
        return envMapping;
    }

    public void setEnvMapping(ImmutableMap<String, String> envMapping) {
        this.envMapping = envMapping;
    }
}
