package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.feature.PushService;
import qunar.tc.qconfig.server.support.AdminUtil;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2017 2017/4/5 19:14
 */
public class PushConfigServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PushConfigServlet.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private PushService pushService;

    private ClientInfoService clientInfoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }

        pushService = context.getBean(PushService.class);
        clientInfoService = context.getBean(ClientInfoService.class);
        Preconditions.checkNotNull(pushService);
        Preconditions.checkNotNull(clientInfoService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!hasPermission()) {
            logger.warn("not qconfig machine try push, group {}, {}:{}", clientInfoService.getGroup(), clientInfoService.getIp(), clientInfoService.getPort());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ConfigMeta meta = new ConfigMeta(req.getParameter(Constants.GROUP_NAME), req.getParameter(Constants.DATAID_NAME), req.getParameter(Constants.PROFILE_NAME));
        long version = Long.parseLong(req.getParameter(Constants.VERSION_NAME));
        Set<IpAndPort> ipAndPorts = parseRequest(req);
        pushService.push(meta, version, ipAndPorts);
    }

    private boolean hasPermission() {
        return Objects.equal(AdminUtil.getAdminApp(), clientInfoService.getGroup());
    }

    private Set<IpAndPort> parseRequest(HttpServletRequest req) throws IOException {
        List<String> list = readLines(req);
        Set<IpAndPort> ipAndPorts = Sets.newHashSetWithExpectedSize(list.size());
        for (String line : list) {
            line = line.trim();
            if (!Strings.isNullOrEmpty(line)) {
                Iterator<String> iterator = COMMA_SPLITTER.split(line).iterator();
                iterator.next();// skip host
                ipAndPorts.add(new IpAndPort(iterator.next(), Integer.parseInt(iterator.next())));
            }
        }
        return ipAndPorts;
    }

    private List<String> readLines(HttpServletRequest req) throws IOException {
        try (final InputStream inputStream = req.getInputStream()) {
            CharSource charSource = new CharSource() {
                @Override
                public Reader openStream() throws IOException {
                    return new InputStreamReader(inputStream, Constants.UTF_8);
                }
            };
            return charSource.readLines();
        }
    }
}
