package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.UnpublisedConfigInfo;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 17:01
 */
public interface CandidateDao {

    void save(Candidate candidate);

    int update(Candidate candidate, StatusType type);

    void insertOrUpdateBeta(Candidate candidate);

    int batchUpdate(List<Candidate> candidateList);

    int batchSave(List<Candidate> candidateList);

    int completeDelete(ConfigMeta meta);

    Candidate find(String group, String dataId, String profile);

    List<Candidate> find(String group, String profile);

    List<Candidate> findWithoutStatus(String group, String profile, StatusType type);

    boolean existInEnvironment(String group, String dataId, Environment environment);

    boolean exist(String group, String dataId);

    boolean existWithoutStatus(String group, String profile, String dataId, StatusType withoutType);

    List<Candidate> find(String group);

    List<Candidate> findByDataId(String group, String dataId);

    List<Candidate> find(String group, String profile, List<StatusType> statusTypes);

    List<UnpublisedConfigInfo> findUnpublished(String group, String profile, List<StatusType> statusTypes);

    List<UnpublisedConfigInfo> findUnpublishedPage(String group, String profile, List<StatusType> statusTypes, int start, int pageSize);

    List<UnpublisedConfigInfo> findUnpublishedWithDataId(String group, String profile, String dataId, List<StatusType> statusTypes);

    List<UnpublisedConfigInfo> findUnpublishedWithDataIdPage(String group, String profile, String dataId, List<StatusType> statusTypes, int start, int pageSize);

    List<UnpublisedConfigInfo> findUnpublishedWithKeyword(String group, String profile, String keyword, List<StatusType> statusTypes);

    List<UnpublisedConfigInfo> findUnpublishedWithKeywordPage(String group, String profile, String keyword, List<StatusType> statusTypes, int start, int pageSize);
}
