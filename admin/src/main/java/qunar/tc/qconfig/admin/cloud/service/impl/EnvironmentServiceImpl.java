package qunar.tc.qconfig.admin.cloud.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.cloud.service.EnvironmentService;
import qunar.tc.qconfig.client.spring.QConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class EnvironmentServiceImpl implements EnvironmentService {

    Logger logger = LoggerFactory.getLogger(EnvironmentServiceImpl.class);

    private final static String DEFAULT_ENVS = "defaultEnvs";

    private final static String ENV_ORDERS = "envOrders";

    private final static ObjectMapper mapper = new ObjectMapper();

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    private volatile List<String> defaultEnvList = ImmutableList.of();

    private volatile Map<String, Integer> envOrderMap = ImmutableMap.of();


    @Override
    public List<String> getSystemDefaultEnvs() {
        return defaultEnvList;
    }

    @Override
    public Map<String, Integer> getEnvDisplayOrders() {
        return envOrderMap;
    }

    @QConfig("environment.properties")
    private void onLoad(Map<String, String> configMap) {

        String defaultEnvsStr = configMap.get(DEFAULT_ENVS);
        if (Strings.isNullOrEmpty(defaultEnvsStr)) {
            defaultEnvList = ImmutableList.of();
        } else {
            defaultEnvList = COMMA_SPLITTER.splitToList(defaultEnvsStr);
        }

        String envOrderJsonStr = configMap.get(ENV_ORDERS);
        if (Strings.isNullOrEmpty(envOrderJsonStr)) {
            envOrderMap = ImmutableMap.of();
        } else {
            try {
                envOrderMap = mapper.readValue(envOrderJsonStr, new TypeReference<Map<String, Integer>>() {
                });
            } catch (IOException e) {
                logger.error("parse env display orders json error", e);
                envOrderMap = ImmutableMap.of();
            }
        }
        logger.info("load qconfig file environment.properties, defaultEnvList[{}], envOrderMap[{}]", defaultEnvList, envOrderMap);
    }
}
