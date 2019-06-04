package qunar.tc.qconfig.admin.event.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.MailUtil;
import qunar.tc.qconfig.admin.event.ReferenceNotifyBean;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.service.UserContextService;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 14:46
 */
// 对被引用方发邮件
@Service
public class ReferenceRefMailListener extends ReferenceMailListener {

    @Resource
    private UserContextService userContext;

    @Override
    protected boolean needMail(ReferenceNotifyBean notifyBean) {
        return super.needMail(notifyBean) && !getRefNamesWithoutSrcNames(notifyBean).isEmpty();
    }

    @Override
    protected Set<String> generateNames(ReferenceNotifyBean notifyBean) {
        return getRefNamesWithoutSrcNames(notifyBean);
    }

    private Set<String> getRefNamesWithoutSrcNames(ReferenceNotifyBean notifyBean) {
        Set<String> sourceGroupNames = userContext.getRelativeMailAddresses(notifyBean.reference.getGroup(), notifyBean.reference.getOperator());
        Set<String> refGroupNames = userContext.getRelativeMailAddresses(notifyBean.reference.getRefGroup(), notifyBean.reference.getOperator());
        refGroupNames.removeAll(sourceGroupNames);
        return refGroupNames;
    }

    @Override
    protected StringBuilder addLink(StringBuilder sb, ReferenceNotifyBean notifyBean) {
        Reference ref = notifyBean.reference;
        return sb.append(String.format(MailUtil.META_LINK_TEMPLATE, ref.getRefGroup(), ref.getRefProfile(), ref.getRefDataId()));
    }
}
