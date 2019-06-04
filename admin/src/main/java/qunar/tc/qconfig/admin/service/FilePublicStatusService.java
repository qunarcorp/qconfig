package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;

import java.util.List;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:55
 */
public interface FilePublicStatusService {

    boolean isReference(ConfigMetaWithoutProfile configMetaWithoutProfile);

    boolean isPublic(ConfigMetaWithoutProfile configMetaWithoutProfile);

    boolean isInherit(ConfigMetaWithoutProfile configMetaWithoutProfile);

    boolean isRest(ConfigMetaWithoutProfile configMetaWithoutProfile);

    void setPublic(ConfigMetaWithoutProfile configMetaWithoutProfile);

    void batchSetPublic(List<CandidateDTO> candidateDTOList);

    void setInherit(ConfigMetaWithoutProfile configMetaWithoutProfile);

    void setRest(ConfigMetaWithoutProfile configMetaWithoutProfile);

    Set<String> getPublicFileNames(String group);

    Set<String> getInheritFileNames(String group);
}
