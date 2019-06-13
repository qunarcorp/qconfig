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


}
