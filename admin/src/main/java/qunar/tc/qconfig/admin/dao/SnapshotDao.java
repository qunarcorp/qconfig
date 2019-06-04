package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.sql.Timestamp;
import java.util.List;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 2:42 PM
 */
public interface SnapshotDao {

    void save(VersionData<ConfigMeta> configId, ChecksumData<String> checksumData, long basedVersion);

    int batchSave(List<CandidateSnapshot> candidateSnapshotList);

    void saveBeta(VersionData<ConfigMeta> configId, ChecksumData<String> checksumData, long basedVersion, Timestamp createTime);

    ChecksumData<String> find(VersionData<ConfigMeta> configId);

    int completeDelete(ConfigMeta meta);

}
