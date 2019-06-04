package qunar.tc.qconfig.demo;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.MapConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * User: zhaohuiyu
 * Date: 5/28/14
 * Time: 3:43 PM
 */
public class TestServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(TestServlet.class);

    private static final long serialVersionUID = -8426390409702088505L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DiscoveryClient discoveryClient = DiscoveryManager.getInstance().getDiscoveryClient();
        List<InstanceInfo> instances = discoveryClient.getApplication("eureka").getInstances();
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~");
        for (InstanceInfo instance : instances) {
            logger.info("host: {}, port: {}, addr: {}", instance.getHostName(), instance.getPort(), instance.getIPAddr());
        }

    }
}
