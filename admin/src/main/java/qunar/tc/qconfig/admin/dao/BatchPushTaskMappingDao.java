package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/5/25 18:43
 */
public interface BatchPushTaskMappingDao {

    boolean insert(ConfigMeta meta, String uuid);

    int batchSave(List<CandidateDTO> candidateDTOList);

    String selectUuid(ConfigMeta meta);

    List<String> selectUuidsUpdateBefore(Timestamp timestamp);

    boolean update(String uuid);

    boolean update(String uuid, int lockVersion);

    boolean update(String dataId, String uuid, int lockVersion);

    boolean delete(ConfigMeta meta, String uuid);

    boolean delete(List<String> uuidList);
}
