package qunar.tc.qconfig.server.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.server.dao.FileDeleteDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/5/15 18:41
 */
@Repository
public class FileDeleteDaoImpl implements FileDeleteDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ConfigMeta> select(String ip) {
        return jdbcTemplate.query("SELECT group_id, data_id, profile FROM file_delete WHERE ip=INET_ATON(?)", CONFIG_META_MAPPER, ip);
    }

    @Override
    public void delete(ConfigMeta meta, String ip) {
        jdbcTemplate.update("DELETE FROM file_delete WHERE group_id=? AND data_id=? AND profile=? AND ip=INET_ATON(?)",
                meta.getGroup(), meta.getDataId(), meta.getProfile(), ip);
    }

    private static final RowMapper<ConfigMeta> CONFIG_META_MAPPER = new RowMapper<ConfigMeta>() {
        @Override
        public ConfigMeta mapRow(ResultSet rs, int i) throws SQLException {
            return new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
        }
    };
}
