package qunar.tc.qconfig.admin.service;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import qunar.tc.qconfig.admin.dto.CandidateDTO;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/3/2 16:26
 */
public interface FileDescriptionService {

    String getDescription(String group, String dataId);

    void batchSetDescription(List<CandidateDTO> candidateDTOList, boolean skipEmptyDesc);

    void setDescription(String group, String dataId, String description);
    //dataId-description
    Map<String, String> getDescriptions(String group);
    // group-dataId-description
    Table<String, String, String> getDescriptions(Multimap<String, String> groupDataIdMappings);
}
