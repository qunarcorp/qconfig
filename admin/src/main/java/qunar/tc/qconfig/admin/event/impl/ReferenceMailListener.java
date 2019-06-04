package qunar.tc.qconfig.admin.event.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import qunar.tc.qconfig.admin.event.MailSender;
import qunar.tc.qconfig.admin.event.ReferenceChangeListener;
import qunar.tc.qconfig.admin.event.ReferenceNotifyBean;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import java.util.Map;
import java.util.Set;

import static qunar.tc.qconfig.admin.event.MailUtil.LINE;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 13:50
 */
public abstract class ReferenceMailListener extends MailSender<ReferenceNotifyBean> implements ReferenceChangeListener {

    private static volatile Set<String> ignoreMailApps = ImmutableSet.of();

    private static final Splitter COMMA_SPLITER = Splitter.on(',').omitEmptyStrings().trimResults();

    // TODO: 2019-05-15 这里的配置需要处理
    static {
        MapConfig mapConfig = MapConfig.get("config.properties", Feature.create().setFailOnNotExists(false).build());
        mapConfig.asMap();
        mapConfig.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
//                ignoreMailApps = ImmutableSet.copyOf(COMMA_SPLITER.split(conf.get("notify.mail.ignore.apps")));
            }
        });
    }

    @Override
    public void referenceChange(ReferenceNotifyBean notifyBean) {
        sendMail(notifyBean);
    }

    @Override
    protected boolean needMail(ReferenceNotifyBean notifyBean) {
        Reference ref=notifyBean.reference;
        if(ProfileUtil.affectProd(ref.getProfile())){
            return ignoreMailApps == null || !ignoreMailApps.contains(ref.getGroup());
        }
        return false;
    }

    @Override
    protected String generateContent(ReferenceNotifyBean notifyBean) {
        Reference ref = notifyBean.reference;
        StringBuilder sb = new StringBuilder();
        if (notifyBean.changeType == RefChangeType.ADD) {
            sb = addLink(sb, notifyBean).append(LINE);
        }
        sb.append("应用：").append(ref.getGroup()).append(LINE);
        sb.append("环境：").append(Environment.fromProfile(ref.getProfile()).text()).append(LINE);
        sb.append(QConfigAttributesLoader.getInstance().getBuildGroup()).append("：").append(ProfileUtil.getBuildGroup(ref.getProfile())).append(LINE);
        sb.append("文件名：").append(ref.getAlias()).append(LINE);
        sb.append("操作类型：").append(getChangeText(notifyBean.changeType)).append(LINE);
        sb.append("被引用应用：").append(ref.getRefGroup()).append(LINE);
        sb.append("被引用环境：").append(Environment.fromProfile(ref.getRefProfile()).text()).append(LINE);
        sb.append("被引用").append(QConfigAttributesLoader.getInstance().getBuildGroup()).append("：").append(ProfileUtil.getBuildGroup(ref.getRefProfile())).append(LINE);
        sb.append("被引用文件名：").append(ref.getRefDataId()).append(LINE);
        sb.append("操作人：").append(notifyBean.reference.getOperator());
        return sb.toString();
    }

    protected abstract StringBuilder addLink(StringBuilder sb, ReferenceNotifyBean notifyBean);

    @Override
    protected String generateTitle(ReferenceNotifyBean notifyBean) {
        Reference ref = notifyBean.reference;
        return String.format(TITLE_TEMPLATE, ref.getGroup(), Environment.fromProfile(ref.getProfile()).text(),
                ProfileUtil.getBuildGroup(ref.getProfile()), ref.getAlias(), getChangeText(notifyBean.changeType),
                ref.getRefGroup(), Environment.fromProfile(ref.getRefProfile()).text(),
                ProfileUtil.getBuildGroup(ref.getRefProfile()), ref.getRefDataId(), notifyBean.reference.getOperator());
    }

    private String getChangeText(RefChangeType changeType) {
        switch (changeType) {
            case ADD:
                return "引用";
            case REMOVE:
                return "取消引用";
            case INHERIT:
                return "继承";
            default:
                throw new IllegalArgumentException("illegal change type: " + changeType);
        }
    }

    public static final String TITLE_TEMPLATE = "配置中心应用 [%s] 环境 [%s] " + QConfigAttributesLoader.getInstance().getBuildGroup() + " [%s] 文件 [%s] [%s] 应用 [%s] 下环境 [%s] " + QConfigAttributesLoader.getInstance().getBuildGroup() + " [%s] 文件 [%s]，操作人 [%s]";
}
