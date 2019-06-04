package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * Created by pingyang.yang on 2018.10.22
 */
public interface FileContentMD5Service {

    void applyConfigChange(CandidateSnapshot snapshot);

    boolean checkContentIsPublished(CandidateSnapshot currentConfig, int publishedVersion);

    String getFileContentMD5(ConfigMeta meta, int version);

    List<ConfigMetaVersion> getConfigMetaByContentMD5(String fileContextMD5);

    int getNewestVersionByMD5(ConfigMeta configMeta, String fileContextMD5);
}
