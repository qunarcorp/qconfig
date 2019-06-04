package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.dao.FileDescriptionDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.service.FileDescriptionService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhenyu.nie created on 2016 2016/3/2 16:28
 */
@Service
public class FileDescriptionServiceImpl implements FileDescriptionService {

    private final Logger logger = LoggerFactory.getLogger(FileDescriptionServiceImpl.class);

    @Resource
    private FileDescriptionDao fileDescriptionDao;

    @Override
    public String getDescription(String group, String dataId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataId));
        String desc = fileDescriptionDao.selectDescription(group, dataId);
        return Strings.nullToEmpty(desc);
    }

    @Override
    public void setDescription(String group, String dataId, String description) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataId));
        description = Strings.nullToEmpty(description);
        fileDescriptionDao.setDescription(group, dataId, description);
    }

    @Override
    public void batchSetDescription(List<CandidateDTO> candidateDTOList, boolean skipEmptyDesc) {
        if (CollectionUtils.isEmpty(candidateDTOList))  return;
        List<CandidateDTO> filteredCandidateDTOList = skipEmptyDesc ? candidateDTOList.stream().filter((dto) ->
                !Strings.isNullOrEmpty(dto.getDescription())).collect(Collectors.toList()) : candidateDTOList;
        fileDescriptionDao.batchSetDescription(filteredCandidateDTOList);
    }

    @Override
    public Map<String, String> getDescriptions(String group) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        return fileDescriptionDao.selectDescriptions(group);
    }

    @Override
    public Table<String, String, String> getDescriptions(Multimap<String, String> groupDataIdMappings) {
        Table<String, String, String> table = HashBasedTable.create();
        for (Map.Entry<String, String> groupDataId : groupDataIdMappings.entries()) {
            table.put(groupDataId.getKey(), groupDataId.getValue(), "");
        }
        for (Map.Entry<String, Collection<String>> groupDataIdMapping : groupDataIdMappings.asMap().entrySet()) {
            Map<String, String> dataIdDescriptions = fileDescriptionDao.selectDescriptions(groupDataIdMapping.getKey(), groupDataIdMapping.getValue());
            for (Map.Entry<String, String> dataIdDesc : dataIdDescriptions.entrySet()) {
                table.put(groupDataIdMapping.getKey(), dataIdDesc.getKey(), dataIdDesc.getValue());
            }
        }
        return table;
    }

}
