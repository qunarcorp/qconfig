package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.FilePublicRecord;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;

public interface BetaConfigUpdateService {

    void syncConfig(CandidateSnapshot snapshot);

    void syncFilePublicStatus(FilePublicRecord record);

    void syncConfigReference(Reference reference);

    void updateBeginIndex(String tableName, long id);
}
