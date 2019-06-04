package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.DefaultTemplateConfigDao;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhenyu.nie created on 2016 2016/10/19 21:25
 */
@Service
public class DefaultTemplateConfigDaoImpl implements DefaultTemplateConfigDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public long insert(final String templateConfig) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement("INSERT INTO default_template_config(config) VALUES(?)", PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, templateConfig);
                return ps;
            }
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public String select(long id) {
        return jdbcTemplate.query("SELECT config FROM default_template_config WHERE id=?", CONFIG_EXTRACTOR, id);
    }

    private static final ResultSetExtractor<String> CONFIG_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("config");
            }
            return null;
        }
    };
}
