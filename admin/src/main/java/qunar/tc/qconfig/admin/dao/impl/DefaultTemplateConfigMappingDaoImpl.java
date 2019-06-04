package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.DefaultTemplateConfigMappingDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhenyu.nie created on 2017 2017/3/15 15:47
 */
@Repository
public class DefaultTemplateConfigMappingDaoImpl implements DefaultTemplateConfigMappingDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public int insert(ConfigMeta meta, long defaultConfigId) {
        return jdbcTemplate.update("INSERT INTO default_template_config_mapping(group_id, data_id, profile, config_id) " +
                "VALUES(?, ?, ?, ?)", meta.getGroup(), meta.getDataId(), meta.getProfile(), defaultConfigId);
    }

    @Override
    public Long select(ConfigMeta meta) {
        return jdbcTemplate.query("SELECT config_id FROM default_template_config_mapping WHERE group_id=? AND data_id=? AND profile=?",
                CONFIG_ID_EXTRACTOR, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public int delete(ConfigMeta meta) {
        return jdbcTemplate.update("DELETE FROM default_template_config_mapping WHERE group_id=? AND data_id=? AND profile=?",
                meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final ResultSetExtractor<Long> CONFIG_ID_EXTRACTOR = new ResultSetExtractor<Long>() {
        @Override
        public Long extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getLong("config_id");
            }
            return null;
        }
    };

}
