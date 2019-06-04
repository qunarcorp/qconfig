package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileContentMD5Dao;
import qunar.tc.qconfig.admin.model.FileContentMD5;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by pingyang.yang on 2018/10/23
 */
@Repository
public class FileContentMD5DaoImpl implements FileContentMD5Dao {

    private static final String INSERT_SQL = "INSERT INTO file_content_md5 (group_id, profile, data_id, version, md5) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_SQL = "SELECT md5 FROM file_content_md5 WHERE group_id = ? AND data_id = ? AND profile = ? AND version = ?";

    private static final String SELECT_BY_MD5_SQL = "SELECT version FROM file_content_md5 WHERE group_id = ? AND data_id = ? AND profile = ? AND md5 = ? LIMIT 1";

    private static final String DELETE_SQL = "DELETE FROM file_content_md5 WHERE group_id = ? AND data_id = ? AND profile = ? ";

    @Resource
    JdbcTemplate jdbcTemplate;

    @Override
    public int insert(FileContentMD5 fileContent) {
        return jdbcTemplate
                .update(INSERT_SQL, fileContent.getGroup(), fileContent.getProfile(), fileContent.getDataId(),
                        fileContent.getVersion(), fileContent.getMd5());
    }

    @Override
    public String selectMD5(ConfigMeta configMeta, int version) {
        return jdbcTemplate.query(SELECT_SQL, MD5_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(),
                configMeta.getProfile(), version);
    }

    @Override
    public int selectVersionByMD5(ConfigMeta configMeta, String MD5) {
        return jdbcTemplate.query(SELECT_BY_MD5_SQL, VERSION_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(),
                configMeta.getProfile(), MD5);
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update(DELETE_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final ResultSetExtractor<String> MD5_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getString("md5");
            }
            return null;
        }
    };

    private static final ResultSetExtractor<Integer> VERSION_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getInt("version");
            }
            return -1;
        }
    };
}
