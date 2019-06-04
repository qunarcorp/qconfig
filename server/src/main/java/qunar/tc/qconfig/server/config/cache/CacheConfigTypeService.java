package qunar.tc.qconfig.server.config.cache;

import com.google.common.base.Optional;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 14:00
 */
public interface CacheConfigTypeService {

    Optional<PublicType> getType(String group, String dataId);

    void update(PublicConfigInfo configInfo);
}
