package qunar.tc.qconfig.server.web;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.common.metrics.Metrics;
import qunar.tc.qconfig.common.metrics.QConfigTimer;
import qunar.tc.qconfig.server.support.context.ClientInfoService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-6-19
 * Time: 下午5:32
 */
public class MonitorFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(MonitorFilter.class);

    private static final String SERVLET_PREFIX = "/client";

    private ClientInfoService clientInfoService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        clientInfoService = context.getBean(ClientInfoService.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        QConfigTimer timer = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String contextPath = request.getServletPath();
            String recordPath = contextPath.substring(SERVLET_PREFIX.length() + 1, contextPath.length());
            if (!Strings.isNullOrEmpty(recordPath)) {
                timer = Metrics.timer(recordPath + ".request." + clientInfoService.getEnv());
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            log.error("monitor servlet error", e);
        } finally {
            if (timer != null) {
                timer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void destroy() {

    }
}
