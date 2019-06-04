package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.PushConfigVersionDao;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 12:09
 */
@Repository
public class PushConfigVersionDaoImpl implements PushConfigVersionDao {

    private static final String SELECT_SQL = "SELECT id, version FROM push_config_version " +
            "WHERE group_id=? AND data_id=? AND profile=? AND version<=?";

    private static final String DELETE_SQL = "DELETE FROM push_config_version WHERE id=? AND version<=?";

    private static final String UPDATE_SQL = "INSERT INTO push_config_version(group_id, data_id, profile, ip, version, create_time) " +
            "VALUES(?,?,?,inet_aton(?),?,now()) ON DUPLICATE KEY UPDATE version=IF(version<?,?,version)";

    @Resource
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Map.Entry<Long, Long>> selectIdAndVersions(ConfigMeta meta, long maxVersion) {
        return jdbcTemplate.query(SELECT_SQL, ID_VERSION_MAPPER,
                meta.getGroup(), meta.getDataId(), meta.getProfile(), maxVersion);
    }


    @Override
    public void delete(List<Map.Entry<Long, Long>> envIdVersions) {
        jdbcTemplate.batchUpdate(DELETE_SQL, createDeletePreparedStatement(envIdVersions));
    }


    @Override
    public void update(final ConfigMeta meta, final List<String> ips, final long version) {
        jdbcTemplate.batchUpdate(UPDATE_SQL, createUpdatePreparedStatement(meta, ips, version));
    }

    private static final RowMapper<Map.Entry<Long, Long>> ID_VERSION_MAPPER = new RowMapper<Map.Entry<Long, Long>>() {
        @Override
        public Map.Entry<Long, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Maps.immutableEntry(rs.getLong("id"), rs.getLong("version"));
        }
    };

    private static BatchPreparedStatementSetter createDeletePreparedStatement(final List<Map.Entry<Long, Long>> idVersions) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, idVersions.get(i).getKey());
                ps.setLong(2, idVersions.get(i).getValue());
            }

            @Override
            public int getBatchSize() {
                return idVersions.size();
            }
        };
    }

    private static BatchPreparedStatementSetter createUpdatePreparedStatement(final ConfigMeta meta,
                                                                              final List<String> ips,
                                                                              final long version) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, meta.getGroup());
                ps.setString(2, meta.getDataId());
                ps.setString(3, meta.getProfile());
                ps.setString(4, ips.get(i));
                ps.setLong(5, version);
                ps.setLong(6, version);
                ps.setLong(7, version);
            }

            @Override
            public int getBatchSize() {
                return ips.size();
            }
        };
    }

}
