package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.PropertiesEntryDiff;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Set;

public interface PropertiesEntryLogService {

    void saveEntryLog(CandidateSnapshot current);

    void deleteEntryLog(ConfigMeta meta);

    PaginationResult<PropertiesEntryDiff> listEntries(Set<String> groups, String profile, String dataId, String key,
                                                      String profileLike, String dataIdLike, String keyLike, int page, int pageSize);

    PaginationResult<PropertiesEntryDiff> listEntryLogs(ConfigMeta meta, String key, int page, int pageSize);
}
