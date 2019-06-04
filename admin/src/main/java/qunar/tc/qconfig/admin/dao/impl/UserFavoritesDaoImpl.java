package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.cloud.enums.UserFavoriteType;
import qunar.tc.qconfig.admin.dao.UserFavoritesDao;
import qunar.tc.qconfig.admin.support.PaginationUtil;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserFavoritesDaoImpl implements UserFavoritesDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final static String INSERT_GROUP = "INSERT IGNORE INTO `user_favorites`(`user`, `group_id`, `type`) VALUES(?,?,?)";
    private final static String INSERT_FILE = "INSERT IGNORE INTO `user_favorites`(`user`, `group_id`, `data_id`, `profile`, `type`) VALUES(?,?,?,?,?)";
    private final static String DELETE_GROUP = "DELETE FROM `user_favorites` WHERE `user`=? AND `group_id`=? AND `type`=?";
    private final static String DELETE_FILE = "DELETE FROM `user_favorites` WHERE `user`=? AND `group_id`=? AND `data_id`=? AND `profile`=? AND `type`=?";
    private final static String DELETE_BY_META = "DELETE FROM `user_favorites` WHERE `group_id`=? AND `data_id`=? AND `profile`=?";
    private final static String SELECT_BY_USER_AND_TYPE = "SELECT `group_id`, `data_id`, `profile` FROM `user_favorites` WHERE `user`=? AND `type`=? limit ?, ?";
    private final static String COUNT_BY_USER_AND_TYPE = "SELECT COUNT(*) AS count FROM `user_favorites` WHERE `user`=? AND `type`=?";
    private final static String COUNT_USER_GROUP = "SELECT COUNT(*) AS count FROM `user_favorites` WHERE `user`=? AND `group_id`=? AND `type`=?";
    private final static String COUNT_USER_META = "SELECT COUNT(*) AS count FROM `user_favorites` WHERE `user`=? AND `group_id`=? AND `data_id`=? AND `profile`=? AND `type`=?";

    @Override
    public List<String> listFavoriteGroups(String user, int page, int pageSize) {
        long offset = PaginationUtil.start(page, pageSize);
        return jdbcTemplate.query(SELECT_BY_USER_AND_TYPE, FAVORITE_GROUPS_MAPPER, user, UserFavoriteType.GROUP.getCode(), offset, pageSize);
    }

    @Override
    public List<ConfigMeta> listFavoriteFiles(String user, int page, int pageSize) {
        long offset = PaginationUtil.start(page, pageSize);
        return jdbcTemplate.query(SELECT_BY_USER_AND_TYPE, FAVORITE_FILES_MAPPER, user, UserFavoriteType.FILE.getCode(), offset, pageSize);
    }

    @Override
    public int countFavoriteItems(String user, UserFavoriteType type) {
        return jdbcTemplate.query(COUNT_BY_USER_AND_TYPE, COUNT_EXTRACTOR, user, type.getCode());
    }

    @Override
    public boolean isFavoriteGroup(String group, String user) {
        return jdbcTemplate.query(COUNT_USER_GROUP, COUNT_EXTRACTOR, user, group, UserFavoriteType.GROUP.getCode()) > 0;
    }

    @Override
    public boolean isFavoriteFile(ConfigMeta meta, String user) {
        return jdbcTemplate.query(COUNT_USER_META, COUNT_EXTRACTOR, user, meta.getGroup(), meta.getDataId(), meta.getProfile(), UserFavoriteType.FILE.getCode()) > 0;
    }

    @Override
    public void insertFavoriteGroup(String group, String user) {
        jdbcTemplate.update(INSERT_GROUP, user, group, UserFavoriteType.GROUP.getCode());
    }

    @Override
    public void insertFavoriteFile(ConfigMeta meta, String user) {
        jdbcTemplate.update(INSERT_FILE, user, meta.getGroup(), meta.getDataId(), meta.getProfile(), UserFavoriteType.FILE.getCode());
    }

    @Override
    public void deleteFavoriteGroup(String group, String user) {
        jdbcTemplate.update(DELETE_GROUP, user, group, UserFavoriteType.GROUP.getCode());
    }

    @Override
    public void deleteFavoriteFile(ConfigMeta meta, String user) {
        jdbcTemplate.update(DELETE_FILE, user, meta.getGroup(), meta.getDataId(), meta.getProfile(), UserFavoriteType.FILE.getCode());
    }

    @Override
    public void deleteFavorites(ConfigMeta meta) {
        jdbcTemplate.update(DELETE_BY_META, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final ResultSetExtractor<Integer> COUNT_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("count");
            } else {
                return 0;
            }
        }
    };

    private static final RowMapper<String> FAVORITE_GROUPS_MAPPER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("group_id");
        }
    };

    private static final RowMapper<ConfigMeta> FAVORITE_FILES_MAPPER = new RowMapper<ConfigMeta>() {
        @Override
        public ConfigMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
        }
    };
}

