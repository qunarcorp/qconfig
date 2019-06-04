package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.model.ApiGroupIdRel;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by chenjk on 2018/1/12.
 */
@Repository
public class ApiGroupIdRelDaoImpl {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void save(ApiGroupIdRel apiGroupIdRel) {
        jdbcTemplate.update("insert into api_groupid_rel (groupid, target_groupid, token) values (?, ?, ?)"
                , apiGroupIdRel.getGroupId(), apiGroupIdRel.getTargetGroupId(), apiGroupIdRel.getToken());
    }

    public void delete(Long id) {
        jdbcTemplate.update("delete from api_groupid_rel where id = ?", id);
    }

    public List<ApiGroupIdRel> query(String term, long start, long offset) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("groupid", "%" + term + "%");
        parameters.addValue("target_groupid", "%" + term + "%");
        parameters.addValue("start", start);
        parameters.addValue("offset", offset);
        if (Strings.isNullOrEmpty(term)) {
            return namedParameterJdbcTemplate.query("select * from api_groupid_rel limit :start, :offset"
                    , parameters, Token_MAPPER);
        } else {
            return namedParameterJdbcTemplate.query("select * from api_groupid_rel where groupid like :groupid or target_groupid like :target_groupid limit :start, :offset"
                    , parameters, Token_MAPPER);
        }
    }

    public long count(String term) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("groupid", "%" + term + "%");
        parameters.addValue("target_groupid", "%" + term + "%");
        if (Strings.isNullOrEmpty(term)) {
            return namedParameterJdbcTemplate.query("select count(*) count from api_groupid_rel"
                    , parameters, COUNT_MAPPER);
        } else {
            return namedParameterJdbcTemplate.query("select count(*) count from api_groupid_rel where groupid like :groupid or target_groupid like :target_groupid"
                    , parameters, COUNT_MAPPER);
        }
    }

    private static final ResultSetExtractor<Long> COUNT_MAPPER = new ResultSetExtractor<Long>() {
        @Override
        public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getLong("count");
            }
            return 0l;
        }
    };

    private static final RowMapper<ApiGroupIdRel> Token_MAPPER = new RowMapper<ApiGroupIdRel>() {
        @Override
        public ApiGroupIdRel mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String groupId = rs.getString("groupid");
            String targetGroupId = rs.getString("target_groupid");
            String token = rs.getString("token");
            Timestamp dataChangeLasttime = rs.getTimestamp("datachange_lasttime");
            return new ApiGroupIdRel(id, groupId, targetGroupId, token, dataChangeLasttime);
        }
    };
}
