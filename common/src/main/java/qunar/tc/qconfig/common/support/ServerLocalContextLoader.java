package qunar.tc.qconfig.common.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.exception.DuplicateConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by pingyang.yang on 2018/11/21
 */
public class ServerLocalContextLoader {

    private static Logger logger = LoggerFactory.getLogger(ServerLocalContextLoader.class);

    public static Map<String, String> loadLocalContext() {
        Map<String, String> result = readLocalConf("app-info.properties");
        if (result.isEmpty()) {
            throw new IllegalArgumentException("没有找到应用配置文件app-info.properties，请查看");
        }
        return result;
    }

    private static ImmutableMap<String, String> readLocalConf(final String filename) {
        final ResourceConfig config = ResourceConfig.getOrNull(filename);
        if (config == null) {
            return ImmutableMap.of();
        } else {
            return ImmutableMap.copyOf(config.getAll());
        }
    }

    private static void forbidDuplicateConfig(final String name) {
        try {
            final List<URL> resources = Collections
                    .list(Thread.currentThread().getContextClassLoader().getResources(name));
            if (resources.size() > 1) {
                logger.error("文件{}只允许有一个，但是发现多个，位置分别为: {}", name, resources);
                throw new DuplicateConfigException("配置文件" + name + "不能存在多个，地址分别为：" + resources);
            }
        } catch (IOException e) {
            // do nothing here
        }
    }

    private static final class ResourceConfig {

        private final Map<String, String> data;

        static ResourceConfig getOrNull(final String name) {
            try {
                return new ResourceConfig(name);
            } catch (DuplicateConfigException e) {
                throw new RuntimeException("检测到重复配置文件", e);
            } catch (Exception e) {
                return null;
            }
        }

        private ResourceConfig(final String name) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "配置文件名不能为空");
            forbidDuplicateConfig(name);

            try (InputStream res = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)){
                if (res == null) {
                    throw new RuntimeException("无法找到配置文件: " + name);
                }
                final Properties prop = new Properties();
                prop.load(res);
                data = fromProperties(prop);
            } catch (Exception e) {
                throw new RuntimeException("无法读取配置文件：" + name, e);
            }
        }

        private Map<String, String> fromProperties(final Properties prop) {
            final Map<String, String> map = Maps.newHashMap();
            for (final String key : prop.stringPropertyNames()) {
                map.put(key, prop.getProperty(key));
            }
            return map;
        }

        Map<String, String> getAll() {
            return Collections.unmodifiableMap(data);
        }
    }
}
