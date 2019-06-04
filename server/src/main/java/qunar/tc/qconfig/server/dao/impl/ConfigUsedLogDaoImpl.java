package qunar.tc.qconfig.server.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.server.bean.LogEntry;
import qunar.tc.qconfig.server.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 15:17
 */
@Repository
public class ConfigUsedLogDaoImpl implements ConfigUsedLogDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void updateRemarks(ConfigMeta realMeta, ConfigMeta sourceMeta, String consumerProfile, String ip, int port, long version, ConfigUsedType type, String remarks) {
        jdbcTemplate.update("INSERT INTO `config_used_log`(group_id,data_id,profile,source_group_id,source_data_id,source_profile,consumer_profile,ip,port,version,config_type,remarks,create_time) " +
                        "VALUES(?,?,?,?,?,?,?,INET_ATON(?),?,?,?,?,now()) " +
                        "ON DUPLICATE KEY UPDATE version=IF(source_profile=?,version,?),config_type=IF(source_profile=?,config_type,?),group_id=?,data_id=?,profile=?,source_profile=?,consumer_profile=?,remarks=?,update_time=now()",
                realMeta.getGroup(), realMeta.getDataId(), realMeta.getProfile(), sourceMeta.getGroup(), sourceMeta.getDataId(), sourceMeta.getProfile(), consumerProfile,
                ip, port, version, type.getCode(), remarks, sourceMeta.getProfile(), version, sourceMeta.getProfile(), type.getCode(),
                realMeta.getGroup(), realMeta.getDataId(), realMeta.getProfile(), sourceMeta.getProfile(), consumerProfile, remarks);
    }


    @Override
    public void update(ConfigMeta realMeta, ConfigMeta sourceMeta, String consumerProfile, String ip, int port, long version, ConfigUsedType type, String remarks) {
        jdbcTemplate.update("INSERT INTO `config_used_log`(group_id,data_id,profile,source_group_id,source_data_id,source_profile,consumer_profile,ip,port,version,config_type,remarks,create_time) " +
                        "VALUES(?,?,?,?,?,?,?,INET_ATON(?),?,?,?,?,now()) " +
                        "ON DUPLICATE KEY UPDATE group_id=?,data_id=?,profile=?,source_profile=?,consumer_profile=?,remarks=?, " +
                        "version=?,config_type=?,update_time=now()",
                realMeta.getGroup(), realMeta.getDataId(), realMeta.getProfile(), sourceMeta.getGroup(), sourceMeta.getDataId(), sourceMeta.getProfile(), consumerProfile,
                ip, port, version, type.getCode(), remarks,
                realMeta.getGroup(), realMeta.getDataId(), realMeta.getProfile(), sourceMeta.getProfile(), consumerProfile, remarks,
                version, type.getCode());
    }

    @Override
    public void delete(ConfigMeta sourceMeta, String ip, int port) {
        jdbcTemplate.update("DELETE FROM `config_used_log` WHERE source_group_id=? AND source_data_id=? AND ip=INET_ATON(?) AND port=?",
                sourceMeta.getGroup(), sourceMeta.getDataId(), ip, port);
    }

    private boolean logentrysIsEmpty(List<LogEntry> logEntries) {
        return (logEntries == null || logEntries.isEmpty());
    }

    @Override
    public void batchSave(List<LogEntry> logEntries) {
        if (logentrysIsEmpty(logEntries)) {
            return;
        }
        String sql = "INSERT INTO `config_used_log` (group_id, data_id, profile, source_group_id, source_data_id, source_profile, consumer_profile, ip, port, version, config_type, remarks, create_time) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, INET_ATON(?), ?, ?, ?, ?, now()) ";
        List<Object[]> params = Lists.newArrayList();
        for (LogEntry logEntry : logEntries) {
            ConfigMeta realMeta = logEntry.getRealMeta();
            ConfigMeta sourceMeta = logEntry.getSourceMeta();
            Log log = logEntry.getLog();
            int configUsedTypeCode = logEntry.getConfigUsedType().getCode();
            Object[] param = new Object[]{
                    realMeta.getGroup(), realMeta.getDataId(), realMeta.getProfile(),
                    sourceMeta.getGroup(), sourceMeta.getDataId(), sourceMeta.getProfile(),
                    log.getProfile(), log.getIp(), log.getPort(),
                    log.getVersion(), configUsedTypeCode, logEntry.getRemarks()
            };
            params.add(param);
        }
        jdbcTemplate.batchUpdate(sql, params);
    }
}
