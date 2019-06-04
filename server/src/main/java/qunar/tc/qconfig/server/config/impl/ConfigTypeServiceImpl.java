package qunar.tc.qconfig.server.config.impl;

import com.google.common.base.Optional;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.server.config.ConfigTypeService;
import qunar.tc.qconfig.server.config.cache.CacheConfigTypeService;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 14:07
 */
@Service
public class ConfigTypeServiceImpl implements ConfigTypeService {

    @Resource
    private CacheConfigTypeService cacheConfigTypeService;

    @Override
    public boolean isPublicConfig(String group, String dataId) {
        Optional<PublicType> type = cacheConfigTypeService.getType(group, dataId);
        return type.isPresent() && type.get().isPublic();
    }

    @Override
    public boolean isInheritConfig(String group, String dataId) {
        Optional<PublicType> type = cacheConfigTypeService.getType(group, dataId);
        return type.isPresent() && type.get().isInherit();
    }

    @Override
    public boolean isRest(String group, String dataId) {
        Optional<PublicType> type = cacheConfigTypeService.getType(group, dataId);
        return type.isPresent() && type.get().isRest();
    }
}
