package qunar.tc.qconfig.common.util;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/**
 * 从本地classpath下读取默认配置
 * <p>
 * Created by chenjk on 2018/5/15.
 */
class DefaultPropertiesLoader {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesLoader.class);

    private final Properties propertiesMap = new Properties();

    private static final DefaultPropertiesLoader DEFAULT_PROPERTIES_LOADER = new DefaultPropertiesLoader();

    public static DefaultPropertiesLoader getInstance() {
        return DEFAULT_PROPERTIES_LOADER;
    }

    private DefaultPropertiesLoader() {
        String defaultPropertiesName = "default.conf";

        URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(defaultPropertiesName);
        if (fileUrl == null) return;
        try (InputStream inputStream = fileUrl.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8)) {
            propertiesMap.load(inputStreamReader);
        } catch (Throwable e) {
            logger.error("init default properties failed", e);
        }
    }

    String getDefaultValue(String defaultPropertiesName) {
        Object defaultValue = propertiesMap.get(defaultPropertiesName);
        if (defaultValue != null) {
            return (String) defaultValue;
        } else {
            return "";
        }
    }
}
