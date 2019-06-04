package qunar.tc.qconfig.server.web.servlet;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.server.domain.RelationMeta;
import qunar.tc.qconfig.server.support.monitor.Monitor;
import qunar.tc.qconfig.server.config.cache.CacheService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/7/4 14:50
 */
public class UpdateReferenceServlet extends HttpServlet {

    private static final long serialVersionUID = 3882134296721181298L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CacheService cacheService;

    private ConfigDao configDao;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (context == null) {
            throw new ServletException("init failed");
        }
        this.cacheService = context.getBean(CacheService.class);
        this.configDao = context.getBean(ConfigDao.class);
        Preconditions.checkNotNull(this.cacheService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RelationMeta relationMeta = getRelation(req);
        logger.debug("received relation changed notify, {}", relationMeta);
        if (!relationMeta.isLegal()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Optional<ReferenceInfo> referenceInfoDetail = configDao.loadReferenceInfo(relationMeta);
        if (!referenceInfoDetail.isPresent()) {
            return;
        }

        try {
            // 关系目前实际上只支持add没有remove
            this.cacheService.updateReferenceCache(referenceInfoDetail.get(), RefChangeType.ADD);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.error("notify updated relation failed, {}", relationMeta, e);
            Monitor.updateRelationError.inc();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private RelationMeta getRelation(HttpServletRequest req) {
        String group = req.getParameter(Constants.GROUP_NAME);
        String dataId = req.getParameter(Constants.DATAID_NAME);
        String profile = req.getParameter(Constants.PROFILE_NAME);
        ConfigMeta source = new ConfigMeta(group, dataId, profile);

        String refGroup = req.getParameter(Constants.REF_GROUP_NAME);
        String refDataId = req.getParameter(Constants.REF_DATAID_NAME);
        String refProfile = req.getParameter(Constants.REF_PROFILE);
        ConfigMeta target = new ConfigMeta(refGroup, refDataId, refProfile);

        return new RelationMeta(source, target);
    }
}
