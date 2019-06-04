package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileCommentDao;
import qunar.tc.qconfig.admin.model.FileComment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FileCommentDapImpl implements FileCommentDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final static String INSERT_SQL = "INSERT INTO `file_comment`(`group_id`, `profile`, `data_id`, `version`, `comment`) " +
            "VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `comment`=?";

    private final static String SELECT_COMMENT_SQL = "SELECT `comment` FROM `file_comment` " +
            "WHERE `group_id`=? AND `profile`=? AND `data_id`=? AND `version`=?";

    private final static String SELECT_BY_META_SQL = "SELECT `version`, `comment` FROM `file_comment` " +
            "WHERE `group_id`=? AND `profile`=? AND `data_id`=?";

    private final static String SELECT_BETWEEN_VERSION_SQL = "SELECT `version`, `comment` FROM `file_comment` " +
            "WHERE `group_id`=? AND `profile`=? AND `data_id`=? AND `version` >=? AND `version` <= ?";

    private final static String DELETE_SQL = "DELETE FROM `file_comment` WHERE `group_id`=? AND `profile`=? AND `data_id`=?";

    @Override
    public int insertOrUpdate(String group, String profile, String dataId, long version, String comment) {
        return jdbcTemplate.update(INSERT_SQL, group, profile, dataId, version, comment, comment);
    }

    @Override
    public String query(String group, String profile, String dataId, long version) {
        return jdbcTemplate.query(SELECT_COMMENT_SQL, COMMENT_EXTRACTOR, group, profile, dataId, version);
    }

    @Override
    public Map<Long, String> query(String group, String profile, String dataId) {
        return jdbcTemplate.query(SELECT_BY_META_SQL, VERSION_COMMENT_MAP_EXTRACTOR, group, profile, dataId);
    }

    @Override
    public Map<Long, String> query(String group, String profile, String dataId, long startVersion, long endVersion) {
        return jdbcTemplate.query(SELECT_BETWEEN_VERSION_SQL, VERSION_COMMENT_MAP_EXTRACTOR,
                group, profile, dataId, startVersion, endVersion);
    }

    @Override
    public int delete(String group, String profile, String dataId) {
        return jdbcTemplate.update(DELETE_SQL, group, profile, dataId);
    }

    private ResultSetExtractor<String> COMMENT_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("comment");
            }
            return null;
        }
    };

    private RowMapper<FileComment> MAPPER = new RowMapper<FileComment>() {
        @Override
        public FileComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            FileComment fileComment = new FileComment();
            fileComment.setConfigMeta(new ConfigMeta(rs.getString("group_id"),
                    rs.getString("data_id"),
                    rs.getString("profile")));
            fileComment.setVersion(rs.getLong("version"));
            fileComment.setComment(rs.getString("comment"));
            return fileComment;
        }
    };

    private ResultSetExtractor<Map<Long, String>> VERSION_COMMENT_MAP_EXTRACTOR = new ResultSetExtractor<Map<Long, String>>() {
        @Override
        public Map<Long, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, String> versionCommentMap = new HashMap<>();
            while (rs.next()) {
                versionCommentMap.put(rs.getLong("version"), rs.getString("comment"));
            }
            return versionCommentMap;
        }
    };
}
