package qunar.tc.qconfig.admin.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.GroupOpLogDao;
import qunar.tc.qconfig.admin.event.ProfileCreatedBean;
import qunar.tc.qconfig.admin.event.ProfileCreatedListener;
import qunar.tc.qconfig.admin.model.GroupOpLog;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 23:07
 */
@Service
public class ProfileLogListerner implements ProfileCreatedListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private GroupOpLogDao groupOpLogDao;

    public void profileCreated(ProfileCreatedBean profileCreatedBean) {
        try {
            String remarks = String.format(REMARKS_TEMPLATE, profileCreatedBean.group,
                    Environment.fromProfile(profileCreatedBean.profile).text(),
                    ProfileUtil.getBuildGroup(profileCreatedBean.profile));

            groupOpLogDao.insert(new GroupOpLog(profileCreatedBean.group, profileCreatedBean.operator,
                    remarks, profileCreatedBean.operatorTime));
        } catch (Exception e) {
            logger.error("log profile created error, {}", profileCreatedBean, e);
        }
    }

    private static final String REMARKS_TEMPLATE = "在 [%s] [%s] 环境新建" + QConfigAttributesLoader.getInstance().getBuildGroup() + " [%s]";
}
