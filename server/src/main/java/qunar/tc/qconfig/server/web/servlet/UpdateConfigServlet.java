package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.server.config.ConfigStore;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-8
 * Time: 下午5:42
 */
public class UpdateConfigServlet extends HttpServlet {
    private static final long serialVersionUID = 8306476891238712168L;

    private static final Logger logger = LoggerFactory.getLogger(UpdateConfigServlet.class);

    private ConfigStore configStore;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }
        this.configStore = context.getBean(ConfigStore.class);
        Preconditions.checkNotNull(this.configStore);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = req.getParameter(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        String profile = req.getParameter(Constants.PROFILE_NAME);
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);

        logger.debug("received config changed notify, {}", meta);
        if (Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId)) {
            Monitor.notifyConfigTrivialCounter.inc();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.configStore.update(meta);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.error("notify updated config error, {}", meta, e);
            Monitor.notifyConfigFailCounter.inc();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            Monitor.notifyConfigTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

}
