package qunar.tc.qconfig.server.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.Numbers;
import qunar.tc.qconfig.server.bean.LogEntry;
import qunar.tc.qconfig.server.dao.ConfigLogDao;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * User: zhaohuiyu
 * Date: 5/21/14
 * Time: 5:04 PM
 */
@Repository
public class ConfigLogDaoImpl implements ConfigLogDao {

    private static final String KEY_LOG_REMARKS_MAX = "log.remarks.max";

    private static final int DEFAULT_REMARKS_MAX = 150;

    private int remarksMax;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void init() {
        MapConfig config = MapConfig.get("config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                remarksMax = Numbers.toInt(conf.get(KEY_LOG_REMARKS_MAX), DEFAULT_REMARKS_MAX);
            }
        });
    }

    @Override
    public void batchSave(List<LogEntry> logEntries) {
        String sql = "INSERT INTO config_log(group_id, data_id, profile, based_version, version, ip, port, record_type, remarks) values(?, ?, ?, ?, ?, INET_ATON(?), ?, ?, ?)";
        if (logEntries != null && !logEntries.isEmpty()) {
            List<Object[]> params = Lists.newLinkedList();
            for (LogEntry logEntry : logEntries) {
                ConfigMeta translatedMeta = logEntry.getRealMeta();
                long basedVersion = logEntry.getBasedVersion();
                Log log = logEntry.getLog();
                String remarks = log.getText();
                if (remarks.length() > remarksMax) {
                    remarks = remarks.substring(0, remarksMax);
                }
                Object[] param = new Object[]{translatedMeta.getGroup(), translatedMeta.getDataId(), translatedMeta.getProfile(),
                        basedVersion, log.getVersion(), log.getIp(), log.getPort(), log.getType().getCode(), remarks};
                params.add(param);
            }
            jdbcTemplate.batchUpdate(sql, params);
        }
    }
}
