package qunar.tc.qconfig.client;

import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/5/23 16:04
 */
public class FeatureManager {

    private final Map<String, String> map;

    FeatureManager(Map<String, String> map) {
        this.map = map;
    }

    public boolean isOn(String key) {
        return Boolean.parseBoolean(map.get(key));
    }
}
