package qunar.tc.qconfig.server.serverself.eureka;

import com.google.common.collect.Lists;
import com.netflix.discovery.DefaultEurekaClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/8/10 12:02
 */
@Service
public class MyEurekaClientConfig extends DefaultEurekaClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(MyEurekaClientConfig.class);

    @Resource
    private ServerStore serverStore;

    @Override
    public List<String> getEurekaServerServiceUrls(String myZone) {
        List<QConfigServer> servers = serverStore.getServers();
        List<String> urls = Lists.newArrayListWithCapacity(servers.size());
        for (QConfigServer server : servers) {
            String url = String.format("http://%s:%s/eureka/", server.getIp(), server.getPort());
            urls.add(url);
        }
        return urls;
    }
}
