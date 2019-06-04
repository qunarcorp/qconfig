package qunar.tc.qconfig.admin.event.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.PublicStatusChangeListener;
import qunar.tc.qconfig.admin.event.PublicStatusNotifyBean;
import qunar.tc.qconfig.admin.event.MailSender;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.client.spring.QMapConfig;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.common.util.Environment;

import javax.annotation.Resource;
import java.util.Set;

import static qunar.tc.qconfig.admin.event.MailUtil.*;

/**
 * @author zhenyu.nie created on 2014 2014/6/27 15:54
 */
@Service
public class PublicStatusMailListner extends MailSender<PublicStatusNotifyBean> implements PublicStatusChangeListener {

    @Resource
    private UserContextService userContext;

    @QMapConfig(value = "config.properties", key = "notify.mail.ignore.apps")
    private Set<String> ignoreMailApps;

    @Override
    public void publicStatusChanged(PublicStatusNotifyBean notifyBean) {
        sendMail(notifyBean);
    }

    @Override
    protected boolean needMail(PublicStatusNotifyBean notifyBean) {
       ConfigMeta meta = notifyBean.configMeta;
        if(!ProfileUtil.affectProd(meta.getProfile())){
            return false;
        }
        return ignoreMailApps == null || !ignoreMailApps.contains(meta.getGroup());
    }

    @Override
    protected String generateContent(PublicStatusNotifyBean notifyBean) {
        ConfigMeta meta = notifyBean.configMeta;
        StringBuilder sb = new StringBuilder();
        String link = String.format(META_LINK_TEMPLATE, meta.getGroup(), meta.getProfile(), meta.getDataId());
        sb.append(link).append(LINE);
        sb.append("应用：").append(meta.getGroup()).append(LINE);
        sb.append("操作类型：").append(notifyBean.event.text()).append(LINE);
        sb.append("环境：").append(Environment.fromProfile(meta.getProfile()).text()).append(LINE);
        sb.append(QConfigAttributesLoader.getInstance().getBuildGroup()).append("：").append(ProfileUtil.getBuildGroup(meta.getProfile())).append(LINE);
        sb.append("文件名：").append(meta.getDataId()).append(LINE);
        sb.append("当前版本：").append(notifyBean.currentVersion).append(LINE);
        sb.append("备注：").append(notifyBean.remarks).append(LINE);
        sb.append("操作人：").append(notifyBean.operator);
        return sb.toString();
    }

    @Override
    protected String generateTitle(PublicStatusNotifyBean notifyBean) {
        ConfigMeta meta = notifyBean.configMeta;
        return String
                .format(TITLE_TEMPLATE, meta.getGroup(), Environment.fromProfile(meta.getProfile()).text(),
                        ProfileUtil.getBuildGroup(meta.getProfile()), meta.getDataId(),
                        notifyBean.event.text(), notifyBean.operator);
    }

    @Override
    protected Set<String> generateNames(PublicStatusNotifyBean notifyBean) {
        return userContext.getRelativeMailAddresses(notifyBean.configMeta.getGroup(), notifyBean.operator);
    }
}
