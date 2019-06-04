package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileValidateUrlDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 16:45
 */
@Repository
public class FileValidateUrlDaoImpl implements FileValidateUrlDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public int update(ConfigMeta meta, String url, String operator) {
        return jdbcTemplate.update("INSERT INTO file_validate_url(group_id, data_id, profile, url, operator, create_time) " +
                "VALUES(?, ?, ?, ?, ?, now()) ON DUPLICATE KEY UPDATE url=?",
                meta.getGroup(), meta.getDataId(), meta.getProfile(), url, operator, url);
    }

    @Override
    public String select(ConfigMeta meta) {
        return jdbcTemplate.query("SELECT url FROM file_validate_url WHERE group_id=? AND data_id=? AND profile=?",
                STRING_EXTRACTOR, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public void delete(ConfigMeta meta) {
        jdbcTemplate.update("DELETE FROM file_validate_url WHERE group_id=? AND data_id=? AND profile=?",
                meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final ResultSetExtractor<String> STRING_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getString("url");
            }
            return null;
        }
    };

}
