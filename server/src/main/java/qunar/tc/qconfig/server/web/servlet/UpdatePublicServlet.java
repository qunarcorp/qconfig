package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.config.cache.CacheConfigTypeService;
import qunar.tc.qconfig.server.dao.FilePublicStatusDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:40
 */
public class UpdatePublicServlet extends HttpServlet {

    private static final long serialVersionUID = 7278623559296127498L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CacheConfigTypeService cacheConfigTypeService;

    private FilePublicStatusDao filePublicStatusDao;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }
        this.cacheConfigTypeService = context.getBean(CacheConfigTypeService.class);
        this.filePublicStatusDao = context.getBean(FilePublicStatusDao.class);
        Preconditions.checkNotNull(this.cacheConfigTypeService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = req.getParameter(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        Optional<PublicConfigInfo> publicConfigInfo = filePublicStatusDao.loadPublicInfo(new ConfigMetaWithoutProfile(group, dataId));
        logger.debug("received public file changed notify, group: [{}], dataId: [{}]", group, dataId);
        if(publicConfigInfo.isPresent()) {
            logger.debug("update public file cache , group: [{}], dataId: [{}]", group, dataId);
            cacheConfigTypeService.update(publicConfigInfo.get());
        }
    }
}
