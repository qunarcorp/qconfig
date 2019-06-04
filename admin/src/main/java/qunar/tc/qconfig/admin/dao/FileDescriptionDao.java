package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.dto.CandidateDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/3/2 16:23
 */
public interface FileDescriptionDao {

    int setDescription(String group, String dataId, String description);

    int[] batchSetDescription(List<CandidateDTO> candidateDTOList);

    String selectDescription(String group, String dataId);

    Map<String, String> selectDescriptions(String group);

    Map<String, String> selectDescriptions(String group, Collection<String> dataIds);
}
