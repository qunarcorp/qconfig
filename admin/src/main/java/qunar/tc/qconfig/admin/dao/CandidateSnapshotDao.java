package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.admin.model.VersionDetail;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: zhaohuiyu
 * Date: 5/23/14
 * Time: 3:15 PM
 */
public interface CandidateSnapshotDao {

    void save(CandidateSnapshot snapshot);

    void saveBeta(CandidateSnapshot snapshot);

    int batchSave(List<CandidateSnapshot> candidateSnapshots);

    CandidateSnapshot find(String group, String dataId, String profile, long editVersion);

    String findOperator(String group, String dataId, String profile, long version);

    List<Long> findVersionsWithStatus(ConfigMeta configMeta, StatusType status);

    List<CandidateSnapshot> findCandidateSnapshots(ConfigMeta configMeta, StatusType status, int limit);

    List<VersionDetail> findVersionsDetailWithStatus(ConfigMeta configMeta, StatusType status);

    Long findEditVersion(ConfigMeta configMeta, long editVersion);

    CandidateSnapshot findLastPublish(ConfigMeta configMeta, long version);

    int completeDelete(ConfigMeta meta);

    CandidateSnapshot findLatestCandidateSnapshot(ConfigMeta configMeta);

    List<CandidateSnapshot> findPublishedCandidateSnapshotsWithApps(List<String> groups, Date beginTime);

    boolean exist(String group, String dataId, int version);

    List<VersionDetail> findVersionsDetailWithBegin(ConfigMeta configMeta, int begin);

    List<CandidateSnapshot> getSnapshotInVersion(ConfigMeta meta, Set<Long> versions);

    List<CandidateSnapshot> getSnapshotAfterVersion(ConfigMeta meta, Long versions);

    /**
     *  按id范围查询，结果包含begin和end
     */
    List<CandidateSnapshot> scanSnapshots(long begin, long end);

    /**
     *  扫描id列表，加锁以保证范围内的事务都已提交，避免遗漏未提交事务
     */
    List<Long> scanIdsWithLock(long begin, long limit);

    long findLatestRecordId();
}