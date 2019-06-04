package qunar.tc.qconfig.admin.support;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 16:00
 */
public class CheckUtil {

    private final static Splitter ACCOUNT_SPLITTER = Splitter.on(":");

    private final static Splitter limitSplitter = ACCOUNT_SPLITTER.trimResults().omitEmptyStrings();

    public static void checkLegalMeta(ConfigMeta meta) {
        checkLegalGroup(meta.getGroup());
        checkLegalProfile(meta.getProfile());
        checkLegalDataId(meta.getGroup());
    }

    public static void checkLegalGroup(String group) {
        checkArgument(isLegalGroup(group), "group不能为空");
    }

    public static void checkLegalDataId(String dataId) {
        checkArgument(!isNullOrEmpty(dataId), "文件名不能为空");
    }

    public static void checkLegalProfile(String profile) {
        checkArgument(ProfileUtil.legalProfile(profile), "无效的 profile: " + profile);
    }

    // 原有的group形式如"tc_qmq_server"和带公司的形式如"qunar:tc_qmq_server"都是合法的group
    public static boolean isLegalGroup(final String maybeConcatGroup) {
        if (Strings.isNullOrEmpty(maybeConcatGroup)) {
            return false;
        }
        List<String> list = limitSplitter.splitToList(maybeConcatGroup);
        return list.size() == 1;
    }

}
