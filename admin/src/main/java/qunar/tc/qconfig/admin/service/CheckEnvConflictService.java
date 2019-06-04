package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Optional;

/**
 * @author zhenyu.nie created on 2014 2014/7/2 17:09
 */
public interface CheckEnvConflictService {

    Optional<Conflict> getConflict(ConfigMeta configMeta);
}
