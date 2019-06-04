package qunar.tc.qconfig.admin.support;

import java.util.List;

/**
 * @author zhenyu.nie created on 2015 2015/2/9 14:12
 */
public class AdminUtil {

    public static <Parent> List<Parent> toSuper(List<? extends Parent> p) {
        return (List<Parent>) p;
    }
}
