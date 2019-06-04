package qunar.tc.qconfig.server.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.server.dao.PushConfigVersionDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PushConfigVersionItem;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 12:18
 */
@Repository
public class PushConfigVersionDaoImpl implements PushConfigVersionDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<PushConfigVersionItem> select() {
        return jdbcTemplate.query("SELECT group_id, data_id, profile, inet_ntoa(ip) as ip, version FROM push_config_version", ITEM_MAPPER);
    }

    @Override
    public PushConfigVersionItem select(ConfigMeta meta, String ip) {
        return jdbcTemplate.query("SELECT group_id, data_id, profile, inet_ntoa(ip) as ip, version FROM push_config_version WHERE group_id=? AND data_id=? AND profile=? AND ip=inet_aton(?)",
                ITEM_EXTRACTOR, meta.getGroup(), meta.getDataId(), meta.getProfile(), ip);
    }

    private static final RowMapper<PushConfigVersionItem> ITEM_MAPPER = new RowMapper<PushConfigVersionItem>() {
        @Override
        public PushConfigVersionItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigMeta meta = new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
            return new PushConfigVersionItem(meta, rs.getString("ip"), rs.getLong("version"));
        }
    };

    private static final ResultSetExtractor<PushConfigVersionItem> ITEM_EXTRACTOR = new ResultSetExtractor<PushConfigVersionItem>() {
        @Override
        public PushConfigVersionItem extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                ConfigMeta meta = new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
                return new PushConfigVersionItem(meta, rs.getString("ip"), rs.getLong("version"));
            }
            return null;
        }
    };
}
