package qunar.tc.qconfig.admin.support;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.model.PropertiesEntry;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.FileChecker;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author keli.wang
 */
public class PropertiesEntryUtil {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesEntryUtil.class);

    // 与数据库中properties_entries表的key的最大长度保持一致，用于过滤掉太长的key
    private static final int MAX_ENTRY_KEY_LENGTH = 100;

    private static List<PropertiesEntry> propertiesToEntries(final Properties properties,
                                                             final CandidateSnapshot snapshot) {
        final List<PropertiesEntry> entries = Lists.newArrayList();
        for (final String key : properties.stringPropertyNames()) {
            if (key.length() > MAX_ENTRY_KEY_LENGTH) {
                // 不记录key过长的条目
                Monitor.PROPERTIES_KEY_TOO_LONG_COUNTER.inc();
                LOG.info("properties key too long. maxLength = {}, key = {}, CandidateSnapshot = {}",
                         MAX_ENTRY_KEY_LENGTH,
                         key,
                         snapshot);
            } else {
                entries.add(new PropertiesEntry(key,
                                                snapshot.getGroup(),
                                                snapshot.getProfile(),
                                                snapshot.getDataId(),
                                                snapshot.getEditVersion(),
                                                properties.getProperty(key)));
            }
        }

        return Collections.unmodifiableList(entries);
    }

    public static List<PropertiesEntry> toEntries(final CandidateSnapshot snapshot) {
        // 非properties文件，返回空map
        if (!FileChecker.isPropertiesFile(snapshot.getDataId())) {
            return Collections.emptyList();
        }

        try {
            final Properties properties = new Properties();
            properties.load(new StringReader(snapshot.getData()));

            return propertiesToEntries(properties, snapshot);
        } catch (IOException e) {
            LOG.error("parse properties content failed. CandidateSnapshot = {}.", snapshot, e);

            return Collections.emptyList();
        }
    }
}
