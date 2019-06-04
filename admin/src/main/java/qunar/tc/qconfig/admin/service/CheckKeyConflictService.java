package qunar.tc.qconfig.admin.service;

import com.google.common.collect.Multimap;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2015 2015/10/21 16:20
 */
public interface CheckKeyConflictService {

    boolean needCheck(String dataId);

    Multimap<String, ConfigMeta> checkKeyConflict(CandidateSnapshot snapshot);
}
