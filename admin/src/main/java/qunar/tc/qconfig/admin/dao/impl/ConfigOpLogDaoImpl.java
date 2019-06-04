package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 22:33
 */
@Repository
public class ConfigOpLogDaoImpl implements ConfigOpLogDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String SELECT_BY_META_SQL = "select group_id, data_id, profile, based_version, edit_version, " +
            "operator, operation_type, remarks, inet_ntoa(ip) as ip, operation_time from config_op_log " +
            "where group_id = ? and data_id = ? and profile = ? and based_version=? " +
            "order by edit_version desc, operation_time desc limit ?, ?";

    private static final String SELECT_BY_GROUP_AND_PROFILE_SQL = "select group_id, data_id, profile, based_version, edit_version, " +
            "operator, operation_type, remarks, inet_ntoa(ip) as ip, operation_time from config_op_log " +
            "where group_id=? and profile=? order by operation_time desc limit ?, ?";

    private static final String SELECT_BY_USER = "SELECT group_id, data_id, profile, based_version, edit_version, operator, " +
            "operation_type, remarks, INET_NTOA(ip) AS ip, operation_time FROM config_op_log WHERE operator=? ORDER BY id DESC LIMIT ?, ?";

    private static final String INSERT_SQL = "insert into config_op_log(" +
            "group_id, data_id, profile, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, inet_aton(?), now())";

    @Override
    public List<ConfigOpLog> selectRecent(ConfigMeta configMeta, long basedVersion, int length) {
        return jdbcTemplate.query(SELECT_BY_META_SQL, CONFIG_OP_LOG_MAPPER, configMeta.getGroup(),
                configMeta.getDataId(), configMeta.getProfile(), basedVersion, 0, length);
    }

    @Override
    public List<ConfigOpLog> selectRecent(String group, String profile, int length) {
        return jdbcTemplate.query(SELECT_BY_GROUP_AND_PROFILE_SQL, CONFIG_OP_LOG_MAPPER, group, profile, 0, length);
    }

    @Override
    public List<ConfigOpLog> selectRecent(String operator, int offset, int length) {
        return jdbcTemplate.query(SELECT_BY_USER, CONFIG_OP_LOG_MAPPER, operator, offset, length);
    }

    @Override
    public int insert(ConfigOpLog configOpLog) {
        return jdbcTemplate.update(INSERT_SQL, configOpLog.getGroup(), configOpLog.getDataId(),
                configOpLog.getProfile(), configOpLog.getBasedVersion(), configOpLog.getEditVersion(),
                configOpLog.getOperator() == null ? "" :  configOpLog.getOperator(), configOpLog.getOperationType().code(), configOpLog.getRemarks(), configOpLog.getIp() == null ? "0.0.0.0": configOpLog.getIp());
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update("DELETE FROM config_op_log WHERE group_id=? AND data_id=? AND profile=?", meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final RowMapper<ConfigOpLog> CONFIG_OP_LOG_MAPPER = new RowMapper<ConfigOpLog>() {
        @Override
        public ConfigOpLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConfigOpLog(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"),
                    rs.getLong("based_version"), rs.getLong("edit_version"), rs.getString("operator"),
                    ConfigOperationEvent.of(rs.getInt("operation_type")), rs.getString("remarks"), rs.getString("ip"),
                    rs.getTimestamp("operation_time"));
        }
    };
}
