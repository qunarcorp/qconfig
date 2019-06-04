package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FixedConsumerVersionDao;
import qunar.tc.qconfig.admin.dto.ConsumerVersionDto;
import qunar.tc.qconfig.admin.model.FixedVersionRecord;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
@Repository
public class FixedConsumerVersionDaoImpl implements FixedConsumerVersionDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String SCAN_SQL = "SELECT id, group_id, data_id, profile, INET_NTOA(ip) AS ip, version, operator," +
            " create_time, update_time FROM fixed_consumer_version WHERE create_time >= ? ORDER BY create_time ASC limit ?";

    private static final String INSERT_SQL = "INSERT INTO fixed_consumer_version(group_id, data_id, profile, ip, version, operator, create_time) VALUES(?, ?, ?, inet_aton(?), ?, ?, now())";

    private static final String INSERT_BETA_SQL = "INSERT INTO fixed_consumer_version(group_id, data_id, profile, ip, " +
            "version, operator, create_time, update_time) VALUES(?, ?, ?, inet_aton(?), ?, ?, ?, ?)";

    private static final String SELECT_SQL = "SELECT inet_ntoa(ip) AS ip, version FROM fixed_consumer_version WHERE group_id=? AND data_id=? AND profile=?";

    private static final String DELETE_SQL = "DELETE FROM fixed_consumer_version WHERE group_id=? AND data_id=? AND profile=? AND ip=inet_aton(?)";

    @Override
    public List<ConsumerVersionDto> find(ConfigMeta configMeta) {
        return jdbcTemplate.query(SELECT_SQL, IP_VERSION_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile());
    }

    @Override
    public List<FixedVersionRecord> scan(long start, long limit) {
        return jdbcTemplate.query(SCAN_SQL, FIXED_VERSION_RECORD_ROW_MAPPER, start, limit);
    }

    @Override
    public int insertOrUpdateBeta(FixedVersionRecord record) {
        ConfigMeta meta = record.getMeta();
        return jdbcTemplate.update(INSERT_BETA_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile(),
                record.getIp(), record.getVersion(), record.getOperator(), record.getCreateTime(), record.getUpdateTime());
    }

    @Override
    public int add(ConfigMeta configMeta, String ip, long version, String operator) {
        return jdbcTemplate.update(INSERT_SQL, configMeta.getGroup(), configMeta.getDataId(),
                configMeta.getProfile(), ip, version, operator);
    }

    @Override
    public int delete(ConfigMeta configMeta, String ip) {
        return jdbcTemplate.update(DELETE_SQL, configMeta.getGroup(), configMeta.getDataId(),
                configMeta.getProfile(), ip);
    }

    private static final RowMapper<ConsumerVersionDto> IP_VERSION_EXTRACTOR = new RowMapper<ConsumerVersionDto>() {
        @Override
        public ConsumerVersionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConsumerVersionDto consumerVersionDto = new ConsumerVersionDto();
            consumerVersionDto.setIp(rs.getString("ip"));
            consumerVersionDto.setVersion(rs.getLong("version"));
            return consumerVersionDto;
        }
    };

    private static final RowMapper<FixedVersionRecord> FIXED_VERSION_RECORD_ROW_MAPPER = new RowMapper<FixedVersionRecord>() {
        @Override
        public FixedVersionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigMeta meta = new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
            return new FixedVersionRecord(rs.getLong("id"), meta, rs.getString("ip"),
                    rs.getLong("version"), rs.getString("operator"),
                    rs.getTimestamp("create_time"), rs.getTimestamp("update_time"));
        }
    };
}
