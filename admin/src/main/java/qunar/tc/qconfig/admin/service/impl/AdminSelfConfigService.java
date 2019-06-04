package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.spring.QConfig;

import java.util.Map;

@Service
public class AdminSelfConfigService {

    @QConfig("config.properties")
    private Map<String, String> configs;

    public String getStringValue(String key, String defaultValue) {
        if (configs.containsKey(key)) {
            return configs.get(key);
        } else {
            return defaultValue;
        }
    }

    public String getStringValue(String key) {
        return configs.get(key);
    }

    public boolean getBooleanVal(String key, boolean defauleValue) {
        String value = getStringValue(key);
        if(Strings.isNullOrEmpty(value)) {
            return defauleValue;
        } else {
            return Boolean.valueOf(value).booleanValue();
        }

    }
}