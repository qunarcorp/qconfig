package qunar.tc.qconfig.server.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.server.dao.FixedConsumerVersionDao;
import qunar.tc.qconfig.server.domain.IpAndVersion;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.MetaIp;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yunfeng.yang
 * @since 2017/5/15
 */
@Repository
public class FixedConsumerVersionDaoImpl implements FixedConsumerVersionDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String SELECT_ONE_SQL = "SELECT version FROM fixed_consumer_version WHERE group_id=? AND data_id=? AND profile=? AND ip=INET_ATON(?)";

    private static final String SELECT_ALL = "SELECT group_id, data_id, profile, INET_NTOA(ip) AS ip, version FROM fixed_consumer_version";

    @Override
    public Long find(ConfigMeta configMeta, String ip) {
        return jdbcTemplate.query(SELECT_ONE_SQL, CONSUMER_VERSION_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), ip);
    }

    @Override
    public Map<MetaIp, Long> queryAll() {
        return jdbcTemplate.query(SELECT_ALL, FIXED_CONSUMER_VERSION_MAPPER);
    }

    private static final ResultSetExtractor<Long> CONSUMER_VERSION_EXTRACTOR = new ResultSetExtractor<Long>() {
        @Override
        public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
            Long result = null;
            if (rs.next()) {
                result = rs.getLong("version");
            }
            return result;
        }
    };

    private static final ResultSetExtractor<Map<MetaIp, Long>> FIXED_CONSUMER_VERSION_MAPPER = new ResultSetExtractor<Map<MetaIp, Long>>() {
        @Override
        public Map<MetaIp, Long> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<MetaIp, Long> result = new HashMap<>();
            while (rs.next()) {
                ConfigMeta meta = new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
                MetaIp metaIp = new MetaIp(meta, rs.getString("ip"));
                long version = rs.getLong("version");
                result.put(metaIp, version);
            }
            return result;
        }
    };
}
