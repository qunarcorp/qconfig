package qunar.tc.qconfig.server.security;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.io.CharSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.TypedConfig;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/11/30 14:40
 */
@Service
public class NoTokenPermissionServiceImpl implements NoTokenPermissionService {

    private static final Logger logger = LoggerFactory.getLogger(NoTokenPermissionServiceImpl.class);

    private volatile NoTokenInfo noTokenInfo;

    @PostConstruct
    public void init() {
        TypedConfig<String> config = TypedConfig.get("no-token-info", TypedConfig.STRING_PARSER);
        config.current();
        config.addListener(new Configuration.ConfigListener<String>() {
            @Override
            public void onLoad(String conf) {
                noTokenInfo = initNoTokenInfo(conf);
                logger.info("change no token info: {}", noTokenInfo);
            }
        });
    }

    private static final Splitter ANGLE_SPLITTER = Splitter.on('>').omitEmptyStrings().trimResults();

    private NoTokenInfo initNoTokenInfo(String conf) {
        if (Strings.isNullOrEmpty(conf)) {
            return new NoTokenInfo(ImmutableSet.<String>of(), ImmutableTable.<String, String, Object>of());
        }

        List<String> lines;
        try {
            lines = CharSource.wrap(conf).readLines();
        } catch (IOException e) {
            // not happen
            throw new RuntimeException(e);
        }

        Set<String> groups = Sets.newHashSet();
        Table<String, String, Object> files = HashBasedTable.create();
        for (String line : lines) {
            List<String> strs = ANGLE_SPLITTER.splitToList(line);
            if (strs.isEmpty()) {
                continue;
            }
            if (strs.size() == 1) {
                groups.add(strs.get(0));
            } else if (strs.size() == 2) {
                files.put(strs.get(0), strs.get(1), Boolean.TRUE);
            } else {
                throw new IllegalArgumentException("can not parse no token info [" + line + "]");
            }
        }
        return new NoTokenInfo(groups, files);
    }

    @Override
    public boolean hasPermission(String group, String dataId) {
        return noTokenInfo.hasPermission(group, dataId);
    }

    private static class NoTokenInfo {
        private final Set<String> groups;
        private final Table<String, String, Object> files;

        public NoTokenInfo(Set<String> groups, Table<String, String, Object> files) {
            this.groups = groups;
            this.files = files;
        }

        public boolean hasPermission(String group, String dataId) {
            return groups.contains(group) || files.contains(group, dataId);
        }

        @Override
        public String toString() {
            return "NoTokenInfo{" +
                    "groups=" + groups +
                    ", files=" + files +
                    '}';
        }
    }
}
