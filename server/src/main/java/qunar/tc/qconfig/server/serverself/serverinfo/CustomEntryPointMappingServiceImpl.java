package qunar.tc.qconfig.server.serverself.serverinfo;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * @author keli.wang
 */
@Service
public class CustomEntryPointMappingServiceImpl implements CustomEntryPointMappingService {
    private static final Logger LOG = LoggerFactory.getLogger(CustomEntryPointMappingServiceImpl.class);

    private static final String CUSTOM_ROOM_HEADER = "X-Src-Room";
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private volatile ImmutableMultimap<String, String> entryPointMapping = ImmutableListMultimap.of();

    @PostConstruct
    public void initCustomMapping() {
        final MapConfig config = MapConfig.get("custom_entrypoint_mapping.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                if (conf != null) {
                    entryPointMapping = buildEntryPointMapping(conf);
                    LOG.info("load custom entry point mapping successOf. mapping={}", entryPointMapping);
                }
            }
        });
    }

    private ImmutableListMultimap<String, String> buildEntryPointMapping(final Map<String, String> conf) {
        final ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();

        for (final String room : conf.keySet()) {
            final String entryPoints = conf.get(room).toLowerCase();
            builder.putAll(room.toLowerCase(), COMMA_SPLITTER.splitToList(entryPoints));
        }

        return builder.build();
    }

    @Override
    public boolean hasCustomMapping(final HttpServletRequest request) {
        final String room = getCustomRoom(request);
        return !Strings.isNullOrEmpty(room) && entryPointMapping.containsKey(room);
    }

    @Override
    public Set<String> getCustomEntryPoints(final HttpServletRequest request) {
        return ImmutableSet.copyOf(entryPointMapping.get(getCustomRoom(request)));
    }

    private String getCustomRoom(final HttpServletRequest request) {
        return Strings.nullToEmpty(request.getHeader(CUSTOM_ROOM_HEADER)).toLowerCase();
    }
}
