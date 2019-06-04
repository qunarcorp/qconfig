package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.admin.model.PublishedConfigInfo;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ReferenceStatus;

import java.sql.Ref;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 14-6-30
 * Time: 下午2:57
 *
 * @author: xiao.liang
 * @description:
 */
public interface ReferenceDao {

    int create(Reference reference);

    int insertBeta(Reference reference);

    List<Reference> scanReference(long start, long limit);

    List<Reference> findEverReferences(String group, String profile, int type);

    List<Reference> findEverReferences(String group, String profile);

    Reference findEverReference(ConfigMeta meta);

    Reference findEverReference(ConfigMeta meta, int type);

    ConfigMeta findReference(ConfigMeta meta);

    ConfigMeta findReference(ConfigMeta meta, int type);

    List<ConfigMeta> findReferences(String group);

    List<Reference> findReferenceInfos(String group);

    Reference findByReferenced(ConfigMeta refMeta);

    List<Reference> findByReferences(Set<String> groups);

    List<Reference> findReferences(String group, String profile);

    Reference findByReferenced(ConfigMeta refMeta, int type);

    List<Reference> searchReferences(Set<String> groups, String profile);

    public List<Reference> searchReferences(Set<String> groups, String profile, int  type);

    List<PublishedConfigInfo> findReferenceDetail(String group, String profile);

    List<PublishedConfigInfo> findReferenceDetailPage(String group, String profile, int start, int pageSize);

    List<PublishedConfigInfo> findReferenceDetail(String group, String profile, String keyword);

    List<PublishedConfigInfo> findReferenceDetailPage(String group, String profile, String keyword, int start, int pageSize);

    List<PublishedConfigInfo> findReferenceDetailByMeta(String group, String profile, String dataId);

    List<PublishedConfigInfo> findReferenceDetailByMetaPage(String group, String profile, String dataId, int start, int pageSize);

//    List<PublishedConfigInfo> findReferenceDetail(String group, String profile, int type);

    int delete(Reference reference);

    int updateStatusFromRefMeta(ConfigMeta refMeta, ReferenceStatus referenceStatus);

    public int updateStatusFromRefMeta(ConfigMeta refMeta, ReferenceStatus referenceStatus, int type);

    int referenceCount(ConfigMeta refMeta);

    int referenceCount(ConfigMeta refMeta, int type);

    int completeDelete(ConfigMeta meta);

    List<CandidateSnapshot> findCurrentSnapshotsInGroup(String group);

    List<CandidateSnapshot> findCurrentSnapshotsInGroup(String group, int type);

    List<Map.Entry<CandidateSnapshot, ConfigMeta>> findCurrentSnapshotsWithSourceInGroup(String group);

    List<Map.Entry<CandidateSnapshot, ConfigMeta>> findCurrentSnapshotsWithSourceInGroup(String group, int type);
}
