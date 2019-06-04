package qunar.tc.qconfig.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.FileDiffInfo;
import qunar.tc.qconfig.admin.model.ProfileInfo;
import qunar.tc.qconfig.admin.model.PublishedConfigInfo;
import qunar.tc.qconfig.admin.support.DiffUtil;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 2:30 PM
 */
public interface ConfigService {

    ConfigInfoWithoutPublicStatus findPublishedConfigWithoutPublicStatus(ConfigMeta configMeta);

    long currentVersionIncludeDeleted(ConfigMeta configMeta);

    CandidateSnapshot publish(CandidateSnapshot snapshot) throws ModifiedException;

    void batchPublish(final List<CandidateSnapshot> snapshotList);

    ProfileInfo getProfileInfo(String group, String profile, String keyword);

    ProfileInfo getProfileInfo(String group, String profile, String dataId, String keyword);

    PublishedConfigInfo getConfigInfo(String group, String profile, String dataId);

    List<PublishedConfigInfo> getConfigInfo(List<ConfigMeta> metas);

    List<PublishedConfigInfo> getPublishedConfig(String group, String profile);

    CandidateSnapshot getCandidateDetails(String group, String dataId, String profile, long editVersion);

    Candidate currentEdit(ConfigMeta configMeta);

    CandidateSnapshot currentEditSnapshot(ConfigMeta configMeta);

    boolean existWithoutStatus(String group, String profile, String dataId, StatusType withoutType);

    Optional<String> checkWithoutPublicFile(String profile, String dataId);

    boolean saveSnapshot(ConfigMeta meta, long version);

    CandidateSnapshot findLastPublish(ConfigMeta configMeta, long version);

    Map.Entry<VersionData<ConfigMeta>, DiffResult<String>> getHtmlDiffToLastPublish(ConfigMeta meta, String data);

    Map.Entry<VersionData<ConfigMeta>, JsonNode> getJsonDiffToLastPublish(ConfigMeta meta, String data);

    VersionData<String> getCurrentPublishedData(ConfigMeta meta);

    String templateDataLongToStr(String group, String dataId, String data);

    List<Map.Entry<VersionData<ConfigMeta>, DiffResult<String>>> getHtmlProdBetaOrBetaProdDiffs(ConfigMeta meta, String data);

    List<Map.Entry<VersionData<ConfigMeta>, JsonNode>> getJsonProdBetaOrBetaProdDiffs(ConfigMeta meta, String data);

    List<VersionData<ConfigMeta>> getMappedConfigs(ConfigMeta meta);

    void delete(CandidateSnapshot candidateSnapshot) throws ModifiedException;

    List<FileDiffInfo> diffProfile(String group, String lProfile, String rProfile, DiffUtil.DiffType diffType);

    List<FileDiffInfo> diffProfileWithUpperLevel(String group, String lProfile, String rProfile);

    List<Candidate> findCandidatesWithGroupAndEnvironment(String group, Set<Environment> environments);

    List<Candidate> findCandidates(String group, String profile);

    ConfigInfoWithoutPublicStatus findConfigWithoutPublicStatus(ConfigMeta configMeta);

    CandidateSnapshot findLastCandidateSnapshot(ConfigMeta configMeta);

    List<CandidateSnapshot> findPublishedCandidateInAppLast30(List<String> groups);


    ProfileInfo getProfileInfoPage(String group, String profile, String dataId, String keyword, int start, int pageSize);

}
