package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.TableBatchOpWhitelistService;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;

import java.util.Map;

/**
 * @author keli.wang
 * @since 2017/4/7
 */
@Service
public class TableBatchOpWhitelistServiceImpl implements TableBatchOpWhitelistService {

    private static final Logger LOG = LoggerFactory.getLogger(TableBatchOpWhitelistServiceImpl.class);
    private static final Splitter WHITELIST_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();

    private volatile ImmutableSet<String> allowBatchOpApps = ImmutableSet.of();

    public TableBatchOpWhitelistServiceImpl() {
        final MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                allowBatchOpApps = ImmutableSet.copyOf(WHITELIST_SPLITTER.split(Strings.nullToEmpty(conf.get("table.batchOp.whitelist"))));
                LOG.info("allowBatchOpApps updated. {}", allowBatchOpApps);
            }
        });
    }

    @Override
    public boolean allowBatchOp(final String appCode) {
        return allowBatchOpApps.contains(appCode);
    }
}
