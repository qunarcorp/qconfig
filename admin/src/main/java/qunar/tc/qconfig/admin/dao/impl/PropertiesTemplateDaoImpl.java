package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.PropertiesTemplateDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhenyu.nie created on 2017 2017/6/2 16:28
 */
@Repository
public class PropertiesTemplateDaoImpl implements PropertiesTemplateDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public String select(ConfigMeta meta) {
        return jdbcTemplate.query("SELECT detail FROM properties_template WHERE group_id=? AND data_id=? AND profile=?",
                DETAIL_EXTRACTOR, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public int update(ConfigMeta meta, String template, String operator) {
        return jdbcTemplate.update("INSERT INTO properties_template(group_id, data_id, profile, detail, operator, create_time) " +
                "VALUES(?, ?, ?, ?, ?, now()) ON DUPLICATE KEY UPDATE detail=?, operator=?",
                meta.getGroup(), meta.getDataId(), meta.getProfile(), template, operator, template, operator);
    }

    private static final ResultSetExtractor<String> DETAIL_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("detail");
            }
            return null;
        }
    };
}
