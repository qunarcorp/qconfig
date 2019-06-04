package qunar.tc.qconfig.admin.web.security;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/11/14 18:41
 */
@Service
public class AdminService {

    private volatile Set<String> admins = ImmutableSet.of();

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @PostConstruct
    public void init() {
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                admins = ImmutableSet.copyOf(COMMA_SPLITTER.split(conf.get("admins")));
            }
        });
    }

    public boolean isAdmin(String username) {
        return admins.contains(username);
    }
}
