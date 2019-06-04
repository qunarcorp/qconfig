package qunar.tc.qconfig.admin.event;

import qunar.tc.qconfig.common.util.QConfigAttributesLoader;

/**
 * @author zhenyu.nie created on 2014 2014/6/27 15:59
 */
public class MailUtil {

    private static final String ADMIN_URL = QConfigAttributesLoader.getInstance().getAdminUrl();

    public static final String TITLE_TEMPLATE = "配置中心应用 [%s] 环境 [%s] " + QConfigAttributesLoader.getInstance().getBuildGroup() + " [%s] 文件 [%s] [%s]，操作人 [%s]";

    public static final String LINE = "<br>";

    public static final String CANDIDATE_LINK_TEMPLATE = "点击查看<a href=\"http://" + ADMIN_URL + "/view/editSnapshot.do?"
            + "group=%s&profile=%s&dataId=%s&editVersion=%s\">快照</a>";

    public static final String META_LINK_TEMPLATE = "点击查看<a href=\"http://" + ADMIN_URL + "/view/currentPublish.do?"
            + "group=%s&profile=%s&dataId=%s\">快照</a>";

    public static final String REF_LINK_TEMPLATE = "点击查看<a href=\"http://" + ADMIN_URL + "/view/viewReference.do?"
            + "group=%s&profile=%s&dataId=%s\">快照</a>";

    public static final String DIFF_LINK_TEMPLATE = "点击查看<a href=\"http://" + ADMIN_URL + "/view/diffWithVersion.do?"
            + "group=%s&dataId=%s&profile=%s&originVersion=%s&dstVersion=%s\">文件diff</a>";

    public static final String APPROVE_LINK_TEMPLATE = "点击<a href=\"http://" + ADMIN_URL + "/view/difffiles.do?"
            + "group=%s&profile=%s&dataId=%s&targetversion=%s\">审核</a>";

    public static final String NEW_CANDIDATE_LINK_TEMPLATE = "<a target='_blank' href=\"http://" + ADMIN_URL + "/webapp/page/#/qconfig/%s/%s/%s?groupName=%s&status=%s&editVersion=%s\">snapshot</a>";
}
