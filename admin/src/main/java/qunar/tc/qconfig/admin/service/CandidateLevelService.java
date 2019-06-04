package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.common.bean.CandidateSnapshot;

import java.util.List;
import java.util.Map;

public interface CandidateLevelService {

    List<CandidateSnapshot> findCandidates(String group);

    Map<String, CandidateSnapshot> findUpperAndSameLevelFiles(List<CandidateSnapshot> filesInGroup, String profile);
}
