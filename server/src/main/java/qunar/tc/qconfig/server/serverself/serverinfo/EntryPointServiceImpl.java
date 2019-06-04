package qunar.tc.qconfig.server.serverself.serverinfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.serverself.eureka.QConfigServer;
import qunar.tc.qconfig.server.support.AddressUtil;
import qunar.tc.qconfig.servercommon.util.RequestUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author keli.wang
 */
@Service
public class EntryPointServiceImpl implements EntryPointService {

    private static final String TYPE_PARAM = "type";

    private static final int HTTPS_PORT = 8443;

    @Resource
    private RegisterService registerService;

    @Resource
    private CustomEntryPointMappingService customEntryPointMappingService;

    @Override
    public List<String> getHttpEntryPoints(HttpServletRequest request) {
        if (customEntryPointMappingService.hasCustomMapping(request)) {
            return shuffle(customEntryPoints(request));
        }

        Set<String> entryPoints = doGetHttpEntryPoints(request);
        return shuffle(entryPoints);
    }

    @Override
    public List<String> getHttpsEntryPoints(HttpServletRequest request) {
        if (customEntryPointMappingService.hasCustomMapping(request)) {
            return shuffle(customEntryPoints(request));
        }

        Set<String> entryPoints = doGetHttpsEntryPoints(request);
        return shuffle(entryPoints);
    }

    private Set<String> doGetHttpEntryPoints(HttpServletRequest request) {
        Set<QConfigServer> servers = getRegisteredEntryPoints(request);
        Set<String> entryPoints = Sets.newHashSetWithExpectedSize(servers.size());
        for (QConfigServer server : servers) {
            entryPoints.add(toEntryPoint(server.getIp(), server.getPort()));
        }
        return entryPoints;
    }

    private Set<String> doGetHttpsEntryPoints(HttpServletRequest request) {
        Set<QConfigServer> servers = getRegisteredEntryPoints(request);
        Set<String> entryPoints = Sets.newHashSetWithExpectedSize(servers.size());
        for (QConfigServer server : servers) {
            entryPoints.add(toEntryPoint(server.getIp(), HTTPS_PORT));
        }
        return entryPoints;
    }

    private Set<String> customEntryPoints(HttpServletRequest request) {
        return customEntryPointMappingService.getCustomEntryPoints(request);
    }

    private <T> List<T> shuffle(Set<T> input) {
        List<T> outPut = Lists.newArrayList(input);
        Collections.shuffle(outPut);
        return outPut;
    }

    private String toEntryPoint(String ip, int port) {
        return ip + ":" + port;
    }

    private Set<QConfigServer> getRegisteredEntryPoints(final HttpServletRequest request) {
        if (shouldListAll(request)) {
            return registerService.all();
        } else {
            final String realIP = RequestUtil.getRealIP(request);
            return registerService.list(ipToRoom(realIP));
        }
    }

    private String ipToRoom(String realIP) {
        return AddressUtil.roomOf(realIP);
    }

    private boolean shouldListAll(final HttpServletRequest request) {
        return request.getParameter(TYPE_PARAM) != null;
    }

}
