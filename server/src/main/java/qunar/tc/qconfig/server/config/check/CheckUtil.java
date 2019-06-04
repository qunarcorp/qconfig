package qunar.tc.qconfig.server.config.check;

import com.google.common.collect.Lists;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/11/1 23:32
 */
public class CheckUtil {

    public static List<Changed> processStringCase(Map<CheckRequest, Changed> map) {
        List<Changed> changes = Lists.newArrayListWithCapacity(map.size());
        for (Map.Entry<CheckRequest, Changed> entry : map.entrySet()) {
            CheckRequest request = entry.getKey();
            Changed change = entry.getValue();
            if (request.getDataId().equals(change.getDataId())) {
                changes.add(change);
            } else {
                changes.add(new Changed(change.getGroup(), request.getDataId(), change.getProfile(), change.getNewestVersion()));
            }
        }
        return changes;
    }
}
