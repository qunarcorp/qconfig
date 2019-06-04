package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.model.ConfigUsedLogStatus;
import qunar.tc.qconfig.admin.model.DbOpType;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 15:45
 */
@Component
public class ConfigUsedLogDaoImpl implements ConfigUsedLogDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ConfigUsedLog> select(String group, String dataId, String profile) {
        return jdbcTemplate
                .query("SELECT group_id,data_id,profile,source_group_id,source_data_id,source_profile,inet_ntoa(ip) AS ip,port,version,config_type,consumer_profile,remarks,update_time FROM config_used_log "
                                + "WHERE group_id=? AND data_id=? AND profile=? AND status=? ORDER BY update_time DESC",
                        rowMapper, group, dataId, profile, ConfigUsedLogStatus.NORMAL.code());
    }

    @Override
    public List<ConfigUsedLog> select(String group, String dataId, String profile, String ip) {
        return jdbcTemplate
                .query("SELECT group_id,data_id,profile,source_group_id,source_data_id,source_profile,inet_ntoa(ip) AS ip,port,version,config_type,consumer_profile,remarks,update_time FROM config_used_log "
                                + "WHERE group_id=? AND data_id=? AND profile=? AND ip = inet_aton(?) AND status=? ORDER BY update_time DESC",
                        rowMapper, group, dataId, profile, ip, ConfigUsedLogStatus.NORMAL.code());
    }

    @Override
    public List<ConfigUsedLog> select(String group, String dataId, String profile, List<ConfigUsedType> configUsedTypes) {
        List<Object> params = Lists.newArrayList();
        params.add(group);
        params.add(dataId);
        params.add(profile);
        params.add(ConfigUsedLogStatus.NORMAL.code());
        for (ConfigUsedType configUsedType : configUsedTypes) {
            params.add(configUsedType.getCode());
        }
        return jdbcTemplate
                .query("SELECT group_id,data_id,profile,source_group_id,source_data_id,source_profile,inet_ntoa(ip) AS ip,port,version,config_type,consumer_profile,remarks,update_time FROM config_used_log "
                                + "WHERE group_id=? and data_id=? and profile=? and status=? and config_type in (" + SQLUtil
                                .generateQuestionMarks(configUsedTypes.size()) + ") order by update_time desc", rowMapper,
                        params.toArray());
    }

    @Override
    public List<ConfigUsedLog> select(String sourceGroupId, String consumerProfile) {
        return jdbcTemplate
                .query("SELECT group_id,data_id,profile,source_group_id,source_data_id,source_profile,inet_ntoa(ip) AS ip,port,version,config_type,consumer_profile,remarks,update_time FROM config_used_log "
                                + "WHERE source_group_id=? AND consumer_profile LIKE ? AND status=? ORDER BY ip DESC",
                        rowMapper, sourceGroupId, consumerProfile + "%", ConfigUsedLogStatus.NORMAL.code());
    }

    @Override
    public ConfigUsedLog selectNewest(String group, String dataId, String profile, String ip) {
        // 只返回第一个有数据的datasource的查询结果，如果一个ip在beta/prod环境都用过resources的文件可能查出来不是最新
        return jdbcTemplate
                .query("SELECT group_id,data_id,profile,source_group_id,source_data_id,source_profile,inet_ntoa(ip) AS ip,port,version,config_type,consumer_profile,remarks,update_time FROM config_used_log "
                                + "WHERE group_id=? AND data_id=? AND profile=? AND ip = inet_aton(?) AND status=? ORDER BY version DESC LIMIT 1",
                        LOG_EXTRACTOR, group, dataId, profile, ip, ConfigUsedLogStatus.NORMAL.code());
    }

    @Override
    public int delete(ConfigMeta configMeta) {
        return jdbcTemplate
                .update("UPDATE config_used_log SET status=? WHERE source_group_id=? AND source_data_id=? AND source_profile=?",
                        ConfigUsedLogStatus.DELETE.code(), configMeta.getGroup(), configMeta.getDataId(),
                        configMeta.getProfile());

    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate
                .update("DELETE FROM config_used_log WHERE source_group_id=? AND source_data_id=? AND source_profile=?",
                        meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private final RowMapper<ConfigUsedLog> rowMapper = new RowMapper<ConfigUsedLog>() {
        @Override
        public ConfigUsedLog mapRow(ResultSet rs, int i) throws SQLException {
            return new ConfigUsedLog(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"),
                    rs.getString("source_group_id"), rs.getString("source_data_id"), rs.getString("source_profile"),
                    rs.getString("ip"), rs.getInt("port"), rs.getInt("version"),
                    ConfigUsedType.codeOf(rs.getInt("config_type")), rs.getString("consumer_profile"),
                    rs.getString("remarks"), rs.getTimestamp("update_time"));
        }
    };

    private static final ResultSetExtractor<ConfigUsedLog> LOG_EXTRACTOR = new ResultSetExtractor<ConfigUsedLog>() {
        @Override
        public ConfigUsedLog extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new ConfigUsedLog(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"),
                        rs.getString("source_group_id"), rs.getString("source_data_id"), rs.getString("source_profile"),
                        rs.getString("ip"), rs.getInt("port"), rs.getInt("version"),
                        ConfigUsedType.codeOf(rs.getInt("config_type")), rs.getString("consumer_profile"),
                        rs.getString("remarks"), rs.getTimestamp("update_time"));
            }
            return null;
        }
    };

}
