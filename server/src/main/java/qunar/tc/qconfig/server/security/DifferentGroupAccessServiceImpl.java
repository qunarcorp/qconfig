package qunar.tc.qconfig.server.security;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.server.config.ConfigTypeService;
import qunar.tc.qconfig.server.exception.AccessForbiddenException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/4/21 19:50
 */
@Service
public class DifferentGroupAccessServiceImpl implements DifferentGroupAccessService {

    private static final Logger logger = LoggerFactory.getLogger(DifferentGroupAccessServiceImpl.class);

    private static final Splitter SPLITTER = Splitter.on(",").trimResults();

    private volatile boolean forbidDifferentGroupAccess = false;

    private volatile Set<String> ignorePublicCheckAppidSet = Sets.newHashSet();

    @Resource
    private ConfigTypeService configTypeService;

    @PostConstruct
    public void init() {
        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                forbidDifferentGroupAccess = Boolean.parseBoolean(conf.get("forbidDifferentGroupAccess"));
                ignorePublicCheckAppidSet.addAll(SPLITTER.splitToList(conf.get("ignore.public.check.appids")));
                logger.info("forbidDifferentGroupAccess is [{}]", forbidDifferentGroupAccess);
            }
        });
    }

    @Override
    public void checkAccessPermission(String clientGroup, String fileGroup, String fileName) throws AccessForbiddenException {
        if (forbidDifferentGroupAccess
                && !Objects.equal(clientGroup, fileGroup)//读取其他应用配置
                && !ignorePublicCheckAppidSet.contains(fileGroup)//忽略是否public校验
                && !configTypeService.isPublicConfig(fileGroup, fileName)) {
            throw new AccessForbiddenException();
        }
    }
}
