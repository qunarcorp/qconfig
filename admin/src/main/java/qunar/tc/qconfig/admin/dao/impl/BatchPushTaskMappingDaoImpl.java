package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.BatchPushTaskMappingDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/5/25 18:48
 */
@Repository
public class BatchPushTaskMappingDaoImpl implements BatchPushTaskMappingDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "INSERT IGNORE INTO batch_push_task_mapping(group_id, data_id, profile, uuid) VALUES (?, ?, ?, ?)";

    private static final String DELETE_BY_UUID_SQL = "DELETE FROM batch_push_task_mapping WHERE uuid IN ";

    @Override
    public boolean insert(ConfigMeta meta, String uuid) {
        return jdbcTemplate.update(INSERT_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile(), uuid) > 0;
    }

    @Override
    public int batchSave(List<CandidateDTO> candidateDTOList) {
        int result = 0;
        List<Object[]> paramList = Lists.newLinkedList();
        for (CandidateDTO candidateDTO : candidateDTOList) {
            Object[] params = new Object[]{candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile(),
                    candidateDTO.getUuid()};
            paramList.add(params);
        }
        int[] resultArray = jdbcTemplate.batchUpdate(INSERT_SQL, paramList);
        for (int i : resultArray) {
            result += i;
        }
        return result;
    }

    @Override
    public String selectUuid(ConfigMeta meta) {
        return jdbcTemplate.query("SELECT uuid FROM batch_push_task_mapping WHERE group_id=? AND data_id=? AND profile=?", UUID_EXTRACTOR, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public List<String> selectUuidsUpdateBefore(Timestamp timestamp) {
        return jdbcTemplate.query("SELECT DISTINCT uuid FROM batch_push_task_mapping WHERE update_time<?", UUID_MAPPER, timestamp);
    }

    @Override
    public boolean update(String uuid) {
        return jdbcTemplate.update("UPDATE batch_push_task_mapping SET lock_version = lock_version + 1 WHERE uuid=?", uuid) > 0;
    }

    @Override
    public boolean update(String uuid, int lockVersion) {
        return jdbcTemplate.update("UPDATE batch_push_task_mapping SET lock_version = lock_version + 1 WHERE uuid=? AND lock_version=?", uuid, lockVersion - 1) > 0;
    }

    @Override
    public boolean update(String dataId, String uuid, int lockVersion) {
        return jdbcTemplate.update("UPDATE batch_push_task_mapping SET lock_version = lock_version + 1 WHERE uuid=? AND lock_version=? AND data_id=?", uuid, lockVersion - 1, dataId) > 0;
    }

    @Override
    public boolean delete(ConfigMeta meta, String uuid) {
        return jdbcTemplate.update("DELETE FROM batch_push_task_mapping WHERE group_id=? AND data_id=? AND profile=? AND uuid=?",
                meta.getGroup(), meta.getDataId(), meta.getProfile(), uuid) > 0;
    }

    @Override
    public boolean delete(List<String> uuidList) {
        return jdbcTemplate.update(DELETE_BY_UUID_SQL + SQLUtil.generateStubs(uuidList.size()), uuidList.toArray()) > 0;
    }

    private static final ResultSetExtractor<String> UUID_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("uuid");
            } else {
                return null;
            }
        }
    };

    private static final RowMapper<String> UUID_MAPPER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("uuid");
        }
    };
}
