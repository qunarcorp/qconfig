package qunar.tc.qconfig.admin.event.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.MailUtil;
import qunar.tc.qconfig.admin.event.ReferenceNotifyBean;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.service.UserContextService;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 14:44
 */
// 对引用方发邮件
@Service
public class ReferenceSourceMailListener extends ReferenceMailListener {

    @Resource
    private UserContextService userContext;

    @Override
    protected Set<String> generateNames(ReferenceNotifyBean notifyBean) {
        return userContext.getRelativeMailAddresses(notifyBean.reference.getGroup(), notifyBean.reference.getOperator());
    }

    @Override
    protected StringBuilder addLink(StringBuilder sb, ReferenceNotifyBean notifyBean) {
        Reference ref = notifyBean.reference;
        return sb.append(String.format(MailUtil.REF_LINK_TEMPLATE, ref.getGroup(), ref.getProfile(), ref.getAlias()));
    }
}
