package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ClientLogDao;
import qunar.tc.qconfig.admin.model.ClientLog;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/9 17:34
 */
@Repository
public class ClientLogDaoImpl implements ClientLogDao {

    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_RECENT_WITH_VERSION = "SELECT based_version, version, inet_ntoa(ip) AS ip, " +
            "record_type, remarks, create_time FROM config_log " +
            "WHERE group_id=? AND profile=? AND data_id=? AND based_version=? ORDER BY id DESC LIMIT ?";

    private static final String DELETE_SQL = "DELETE FROM config_log WHERE group_id=? AND data_id=? AND profile=?";

    private static final String BATCH_DELETE_SQL = "DELETE FROM config_log WHERE id in (:ids)";

    private static final String SELECT_IDS_SQL = "SELECT id FROM config_log WHERE create_time<=? LIMIT ?";

    private static final RowMapper<ClientLog> CLIENT_LOG_MAPPER = new RowMapper<ClientLog>() {
        @Override
        public ClientLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ClientLog(rs.getString("ip"), rs.getInt("based_version"), rs.getInt("version"),
                    ConfigLogType.codeOf(rs.getInt("record_type")), rs.getString("remarks"),
                    rs.getTimestamp("create_time"));
        }
    };

    @Value("${clientLog.showLength}")
    private int showLength;

    @Override
    public List<ClientLog> selectRecent(String group, String profile, String dataId, long basedVersion) {
        return jdbcTemplate.query(SELECT_RECENT_WITH_VERSION, CLIENT_LOG_MAPPER,
                group, profile, dataId, basedVersion, showLength);
    }

    @Override
    public List<Long> selectIds(DbEnv env, String endTime, int limit) {
        return jdbcTemplate.query(SELECT_IDS_SQL, ID_MAPPER, endTime, limit);
    }

    @Override
    public int delete(ConfigMeta meta) {
        return jdbcTemplate.update(DELETE_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public int delete(DbEnv env, List<Long> ids) {
        MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);
        return namedParameterJdbcTemplate.update(BATCH_DELETE_SQL, params);
    }

    private static final RowMapper<Long> ID_MAPPER = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong("id");
        }
    };

}
