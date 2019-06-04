package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileDescriptionDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.support.SQLUtil;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/3/2 16:24
 */
@Repository
public class FileDescriptionDaoImpl implements FileDescriptionDao {

    private final static String EMPTY_PROFILE = "";
    private final static int EMPTY_VERSION = 0;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final static String INSERT_SQL = "INSERT INTO file_description(group_id, profile, data_id, version, description) " +
            "VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE description=?";

    private final static String SELECT_SQL = "SELECT description FROM file_description " +
            "WHERE group_id=? AND data_id=? AND profile=? AND version=?";

    private final static String SELECT_GROUP_SQL = "SELECT data_id, description FROM file_description " +
            "WHERE group_id=? AND profile=? AND version=?";

    private final static String SELECT_IN_DATAIDS_SQL = "SELECT data_id, description FROM file_description " +
            "WHERE group_id=? AND profile=? AND version=? AND data_id IN ";

    @Override
    public int[] batchSetDescription(List<CandidateDTO> candidateDTOList) {
        List<Object[]> paramList = Lists.newLinkedList();
        for (CandidateDTO candidateDTO : candidateDTOList) {
            String description = Strings.nullToEmpty(candidateDTO.getDescription());
            Object[] params = new Object[]{
                    candidateDTO.getGroup(), EMPTY_PROFILE, candidateDTO.getDataId(),
                    EMPTY_VERSION, description, description};
            paramList.add(params);
        }
        return jdbcTemplate.batchUpdate(INSERT_SQL, paramList);
    }

    @Override
    public int setDescription(String group, String dataId, String description) {
        return jdbcTemplate.update(INSERT_SQL, group, EMPTY_PROFILE, dataId, EMPTY_VERSION, description, description);
    }

    @Override
    public String selectDescription(String group, String dataId) {
        return jdbcTemplate.query(SELECT_SQL, DESCRIPTION_EXTRACTOR, group, dataId, EMPTY_PROFILE, EMPTY_VERSION);
    }

    @Override
    public Map<String, String> selectDescriptions(String group) {
        return jdbcTemplate.query(SELECT_GROUP_SQL, FILE_DESCRIPTION_EXTRACTOR, group, EMPTY_PROFILE, EMPTY_VERSION);
    }


    @Override
    public Map<String, String> selectDescriptions(String group, Collection<String> dataIds) {
        Object[] params = new Object[dataIds.size() + 3];
        params[0] = group;
        params[1] = EMPTY_PROFILE;
        params[2] = EMPTY_VERSION;
        System.arraycopy(dataIds.toArray(), 0, params, 3, dataIds.size());
        return jdbcTemplate.query(SELECT_IN_DATAIDS_SQL + SQLUtil.generateStubs(dataIds.size()), FILE_DESCRIPTION_EXTRACTOR, params);
    }

    private static final ResultSetExtractor<String> DESCRIPTION_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("description");
            }
            return null;
        }
    };

    private static final ResultSetExtractor<Map<String, String>> FILE_DESCRIPTION_EXTRACTOR = new ResultSetExtractor<Map<String, String>>() {
        @Override
        public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<String, String> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("data_id"), rs.getString("description"));
            }
            return result;
        }
    };
}
