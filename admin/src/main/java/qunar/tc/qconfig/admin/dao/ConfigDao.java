package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.PublishedConfigInfo;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PublicStatus;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 2:41 PM
 */
public interface ConfigDao {

    void create(VersionData<ConfigMeta> configId);

    int batchSave(List<CandidateSnapshot> snapshotList);

    int update(VersionData<ConfigMeta> configId, long oldVersion, PublicStatus publicStatus);

    int batchUpdate(List<CandidateSnapshot> candidateSnapshotList);

    ConfigInfoWithoutPublicStatus findCurrentConfigInfo(ConfigMeta configMeta);

    List<PublishedConfigInfo> findPublished(String group, String profile);

    List<PublishedConfigInfo> findPublished(String group, String profile, String dataId);

    List<PublishedConfigInfo> findPublishedPage(String group, String profile, String dataId, int start, int pageSize);

    List<PublishedConfigInfo> findPublishedPage(String group, String profile, int start, int pageSize);

    List<PublishedConfigInfo> findPublished(List<ConfigMeta> metas);

    List<PublishedConfigInfo> findPublishedWithKeyword(String group, String profile, String keyword);

    List<PublishedConfigInfo> findPublishedWithKeywordPage(String group, String profile, String keyword, int start, int pageSize);

    List<ConfigInfoWithoutPublicStatus> findPublicedConfigsInProfileAndResources(Environment environment);

    List<String> findPublicGroupByDataId (String dataId);

    List<ConfigInfoWithoutPublicStatus> findPublicConfigsInProfileAndResources(Environment environment, Set<String> groups,
                                                                               String groupLike, String dataIdLike, int page, int pageSize);

    int countPublicConfigsInProfileAndResources(Environment environment, Set<String> groups, String groupLike,
                                                String dataIdLike, int page, int pageSize);

    List<VersionData<ConfigMeta>> findInEnvironment(String group, String dataId, Environment environment);

    int delete(VersionData<ConfigMeta> configId, long oldVersion);

    List<CandidateSnapshot> findCurrentSnapshotsInGroup(String group);

    int countCurrentSnapshots();

    int countPublicFile(String group, String profile, String keyWord);

    int countUnpublicFile(String group, String profile, String keyWord);

    int countReferenceFile(String group, String profile, String keyWord);

    List<CandidateSnapshot> findCurrentSnapshots(final int offset, final int limit);

    CandidateSnapshot findCurrentSnapshot(final ConfigMeta configMeta);

    int completeDelete(ConfigMeta meta);


}
