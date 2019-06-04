package qunar.tc.qconfig.admin.event.impl;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.GroupOpLogDao;
import qunar.tc.qconfig.admin.event.PermissionChangeListener;
import qunar.tc.qconfig.admin.event.PermissionNotifyBean;
import qunar.tc.qconfig.admin.model.GroupOpLog;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 14:47
 */
@Service
public class PermissionLogListener implements PermissionChangeListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private GroupOpLogDao logDao;

    @Override
    public void permissionChange(PermissionNotifyBean notifyBean) {
        try {
            logDao.insert(new GroupOpLog(notifyBean.group, notifyBean.operator, COMMA_JOINER.join(notifyBean.remarks),
                    notifyBean.opTime));
        } catch (Exception e) {
            logger.error("record permission log error, {}", notifyBean, e);
        }
    }

    private static final Joiner COMMA_JOINER = Joiner.on(',');
}
