package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ConfigEditorSettingsDao;
import qunar.tc.qconfig.admin.model.ConfigEditorSettings;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author keli.wang
 * @since 2017/5/15
 */
@Repository
public class ConfigEditorSettingsDaoImpl implements ConfigEditorSettingsDao {
    private static final String SAVE_OR_UPDATE_SQL = "INSERT INTO `config_editor_settings`(`group_id`,`data_id`,`use_advanced_editor`)" +
            "VALUES (:groupId,:dataId,:useAdvancedEditor) ON DUPLICATE KEY UPDATE use_advanced_editor=:useAdvancedEditor";
    private static final String QUERY_SETTINGS_SQL = "SELECT `use_advanced_editor` FROM config_editor_settings WHERE group_id=:groupId AND data_id=:dataId";

    private static final RowMapper<ConfigEditorSettings> SETTINGS_MAPPER = new RowMapper<ConfigEditorSettings>() {
        @Override
        public ConfigEditorSettings mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ConfigEditorSettings(rs.getBoolean("use_advanced_editor"));
        }
    };

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public int saveOrUpdate(final String groupId, final String dataId, final ConfigEditorSettings settings) {
        final Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("dataId", dataId);
        params.put("useAdvancedEditor", settings.isUseAdvancedEditor());
        return namedParameterJdbcTemplate.update(SAVE_OR_UPDATE_SQL, params);
    }

    @Override
    public ConfigEditorSettings query(final String groupId, final String dataId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("dataId", dataId);
        return namedParameterJdbcTemplate.queryForObject(QUERY_SETTINGS_SQL, params, SETTINGS_MAPPER);
    }
}
