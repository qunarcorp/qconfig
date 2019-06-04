package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.FileContentMD5Dao;
import qunar.tc.qconfig.admin.dao.SnapshotDao;
import qunar.tc.qconfig.admin.model.FileContentMD5;
import qunar.tc.qconfig.admin.service.FileContentMD5Service;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Created by pingyang.yang on 2018/10/24
 */
@Service
public class FileContentMD5ServiceImpl implements FileContentMD5Service {

    @Resource
    FileContentMD5Dao fileContentMD5Dao;

    @Resource
    SnapshotDao configSnapshotDao;

    @Override
    public void applyConfigChange(CandidateSnapshot snapshot) {
        FileContentMD5 fileContentMD5 = new FileContentMD5(snapshot.getGroup(), snapshot.getProfile(),
                snapshot.getDataId(), (int) snapshot.getEditVersion(),
                ChecksumAlgorithm.getChecksum(snapshot.getData()));
        fileContentMD5Dao.insert(fileContentMD5);
    }

    @Override
    public boolean checkContentIsPublished(CandidateSnapshot currentConfig, int publishedVersion) {
        Preconditions.checkNotNull(currentConfig, "candidate is null");

        String fileMD5 = fileContentMD5Dao.selectMD5(new ConfigMeta(currentConfig.getGroup(), currentConfig.getDataId(), currentConfig.getProfile()), publishedVersion);
        return Objects.equals(fileMD5, ChecksumAlgorithm.getChecksum(currentConfig.getData()));
    }

    @Override
    public String getFileContentMD5(ConfigMeta meta, int version) {
        String md5_Context = fileContentMD5Dao.selectMD5(meta, version);
        if (Strings.isNullOrEmpty(md5_Context)) {
            ChecksumData<String> checksumData = configSnapshotDao.find(VersionData.of(version, meta));
            if (checksumData != null) {
                md5_Context = checksumData.getCheckSum();
            }
        }
        return md5_Context;
    }

    @Override
    public List<ConfigMetaVersion> getConfigMetaByContentMD5(String fileContextMD5) {
        return null;
    }

    @Override
    public int getNewestVersionByMD5(ConfigMeta configMeta, String fileContextMD5) {
        return fileContentMD5Dao.selectVersionByMD5(configMeta, fileContextMD5);
    }
}
