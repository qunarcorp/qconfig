package qunar.tc.qconfig.admin.event.impl;

import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.MailSender;
import qunar.tc.qconfig.admin.event.PermissionChangeListener;
import qunar.tc.qconfig.admin.event.PermissionNotifyBean;
import qunar.tc.qconfig.admin.service.UserContextService;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 14:50
 */
@Service
public class PermissionMailListener extends MailSender<PermissionNotifyBean> implements PermissionChangeListener {

    @Resource
    private UserContextService userContext;

    @Override
    public void permissionChange(PermissionNotifyBean notifyBean) {
        sendMail(notifyBean);
    }

    @Override
    protected String generateContent(PermissionNotifyBean notifyBean) {
        return LINE_JOINER.join(notifyBean.remarks);
    }

    @Override
    protected String generateTitle(PermissionNotifyBean notifyBean) {
        return String.format(TITLE_TEMPLATE, notifyBean.group);
    }

    @Override
    protected Set<String> generateNames(PermissionNotifyBean notifyBean) {
        return userContext.getRelativeMailAddresses(notifyBean.group, notifyBean.operator);
    }

    @Override
    protected boolean needMail(PermissionNotifyBean permissionNotifyBean) {
        return true;
    }

    private static final String TITLE_TEMPLATE = "配置中心应用 [%s] 权限变更";

    private static final String LINE = "<br>";

    private static final Joiner LINE_JOINER = Joiner.on(LINE);
}
