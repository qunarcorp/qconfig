package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.dao.PropertiesEntryLogDao;
import qunar.tc.qconfig.admin.model.KeyValuePair;
import qunar.tc.qconfig.admin.model.PropertiesEntryDiff;
import qunar.tc.qconfig.admin.support.PaginationUtil;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public class PropertiesEntryLogDaoImpl implements PropertiesEntryLogDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO `properties_entries_log`(`group_id`, `data_id`, `profile`, `entry_key`, `version`, `value`, `last_version`, `last_value`, `comment`,  `type`, `operator`) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

    private static final String DELETE_BY_META = "DELETE FROM `properties_entries_log` WHERE group_id=? AND data_id=? AND profile=?";

    private static final String DELETE_BY_KEY = DELETE_BY_META + " AND `entry_key`=?";

    private static final String SELECT_ALL = "SELECT `id`, `group_id`, `data_id`, `profile`, `entry_key`, `version`, `value`, `last_version`, `last_value`, `comment`, `type`, `operator`, `create_time` FROM `properties_entries_log`";

    private static final String SELECT_BY_CONFIG_META_SQL = SELECT_ALL + " WHERE `group_id`=? AND `profile`=? AND `data_id`=?";

    private final static String SELECT_BY_KEY = SELECT_ALL + " WHERE `group_id`=:group AND `data_id`=:dataId AND `profile`=:profile AND `entry_key`=:key ORDER BY `id` DESC LIMIT :offset, :limit";

    private final static String COUNT_BY_KEY = "SELECT COUNT(*) AS `total_count` FROM `properties_entries_log` WHERE `group_id`=:group AND `data_id`=:dataId AND `profile`=:profile AND `entry_key`=:key";

    private static final String SELECT_SQL_TEMPLATE = SELECT_ALL + " WHERE `group_id` IN (:groups) %s LIMIT :offset, :limit";

    private static final String SELECT_COUNT_SQL_TEMPLATE = "SELECT COUNT(DISTINCT `group_id`, `data_id`, `profile`, `entry_key`) AS `total_count` FROM `properties_entries_log` WHERE `group_id` IN (:groups) %s";

    private static final String COUNT_BY_META = "SELECT COUNT(*) AS `total_count` FROM `properties_entries_log` WHERE `group_id`=? AND `data_id`=? AND `profile`=?";

    private static final String SELECT_LATEST_IDS_OF_DISTINCT_KEYS = "SELECT MAX(`id`) AS `id` FROM `properties_entries_log` WHERE `group_id` IN (:groups) %s " +
            " GROUP BY `group_id`, `data_id`, `profile`, `entry_key` ORDER BY `id` DESC LIMIT :offset, :limit";

    private static final String SELECT_BY_IDS = SELECT_ALL + " WHERE `id` IN (:ids) ORDER BY `id` DESC";

    @Override
    public void insert(PropertiesEntryDiff entry) {
        jdbcTemplate.update(INSERT_SQL, entry.getGroupId(), entry.getDataId(), entry.getProfile(), entry.getVersion(), entry.getKey(), entry.getValue());
    }

    @Override
    public void batchInsert(final List<PropertiesEntryDiff> entries) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PropertiesEntryDiff entry = entries.get(i);
                ps.setString(1, entry.getGroupId());
                ps.setString(2, entry.getDataId());
                ps.setString(3, entry.getProfile());
                ps.setString(4, entry.getKey());
                ps.setLong(5, entry.getVersion());
                ps.setString(6, entry.getValue());
                ps.setLong(7, entry.getLastVersion());
                ps.setString(8, entry.getLastValue());
                ps.setString(9, entry.getComment());
                ps.setInt(10, entry.getType().getCode());
                ps.setString(11, entry.getOperator());
            }
            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }

    @Override
    public void delete(ConfigMeta meta) {
        jdbcTemplate.update(DELETE_BY_META, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public void delete(PropertiesEntryDiff entry) {
        jdbcTemplate.update(DELETE_BY_KEY, entry.getGroupId(), entry.getDataId(), entry.getProfile(), entry.getKey());
    }

    @Override
    public List<PropertiesEntryDiff> selectByConfigMeta(ConfigMeta meta) {
        return jdbcTemplate.query(SELECT_BY_CONFIG_META_SQL, ENTRY_ROW_MAPPER, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public List<Long> selectLatestIdsOfDistinctKeys(Set<String> groups, String profile, String dataId, String key,
                                                    String profileLike, String dataIdLike, String keyLike, int page, int pageSize) {
        if (CollectionUtils.isEmpty(groups)) {
            return ImmutableList.of();
        }
        KeyValuePair<StringBuilder, MapSqlParameterSource> sqlAndParams = genExtraQueryStringAndParams(groups, profile, dataId, key, profileLike, dataIdLike, keyLike);
        long offset = PaginationUtil.start(page, pageSize);
        String sql = sqlAndParams.getKey().toString();
        MapSqlParameterSource params = sqlAndParams.getValue();
        params.addValue("offset", offset);
        params.addValue("limit", pageSize);
        return namedParameterJdbcTemplate.query(String.format(SELECT_LATEST_IDS_OF_DISTINCT_KEYS, sql), params, ID_ROW_MAPPER);
    }

    @Override
    public List<PropertiesEntryDiff> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ImmutableList.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);
        return namedParameterJdbcTemplate.query(SELECT_BY_IDS, params, ENTRY_ROW_MAPPER);
    }

    @Override
    public int countDistinctKeys(Set<String> groups, String profile, String dataId, String key, String profileLike, String dataIdLike, String keyLike) {
        KeyValuePair<StringBuilder, MapSqlParameterSource> sqlAndParams = genExtraQueryStringAndParams(groups, profile, dataId, key, profileLike, dataIdLike, keyLike);
        String sql = sqlAndParams.getKey().toString();
        return namedParameterJdbcTemplate.query(String.format(SELECT_COUNT_SQL_TEMPLATE, sql), sqlAndParams.getValue(), COUNT_EXTRACTOR);
    }

    @Override
    public List<PropertiesEntryDiff> selectKey(ConfigMeta meta, String key, int page, int pageSize) {
        long offset = PaginationUtil.start(page, pageSize);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("group", meta.getGroup());
        params.addValue("dataId", meta.getDataId());
        params.addValue("profile", meta.getProfile());
        params.addValue("key", key);
        params.addValue("offset", offset);
        params.addValue("limit", pageSize);
        return namedParameterJdbcTemplate.query(SELECT_BY_KEY, params, ENTRY_ROW_MAPPER);
    }

    @Override
    public int countKey(ConfigMeta meta, String key) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("group", meta.getGroup());
        params.addValue("dataId", meta.getDataId());
        params.addValue("profile", meta.getProfile());
        params.addValue("key", key);
        return namedParameterJdbcTemplate.query(COUNT_BY_KEY, params, COUNT_EXTRACTOR);
    }

    @Override
    public boolean isMetaLogExist(ConfigMeta meta) {
        return jdbcTemplate.query(COUNT_BY_META, COUNT_EXTRACTOR, meta.getGroup(), meta.getDataId(), meta.getProfile()) > 0;
    }

    // profile/dataId/key参数非空时，为精确搜索条件, 若为空则用非空的*Like参数作为模糊搜索条件，若*Like也为空，则不限定该条件
    private KeyValuePair<StringBuilder, MapSqlParameterSource> genExtraQueryStringAndParams(
            Set<String> groups, String profile, String dataId, String key, String profileLike, String dataIdLike, String keyLike) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("groups", groups);
        final StringBuilder extraQueryParam = new StringBuilder();
        if (!Strings.isNullOrEmpty(profile)) {
            extraQueryParam.append(" AND `profile`=:profile ");
            params.addValue("profile", profile);
        } else if (!Strings.isNullOrEmpty(profileLike)) {
            extraQueryParam.append(" AND `profile` Like :profileLike ");
            params.addValue("profileLike", "%" + SQLUtil.escapeWildcards(profileLike) + "%");
        }
        if (!Strings.isNullOrEmpty(dataId)) {
            extraQueryParam.append(" AND `data_id`=:dataId ");
            params.addValue("dataId", dataId);
        } else if (!Strings.isNullOrEmpty(dataIdLike)) {
            extraQueryParam.append(" AND `data_id` LIKE :dataIdLike ");
            params.addValue("dataIdLike", "%"+ SQLUtil.escapeWildcards(dataIdLike) + "%");
        }
        if (!Strings.isNullOrEmpty(key)) {
            extraQueryParam.append(" AND `entry_key`=:key ");
            params.addValue("key", key);
        } else if (!Strings.isNullOrEmpty(keyLike)) {
            extraQueryParam.append(" AND `entry_key` LIKE :keyLike ");
            params.addValue("keyLike", "%" + SQLUtil.escapeWildcards(keyLike) + "%");
        }
        return new KeyValuePair<>(extraQueryParam, params);
    }

    private static final RowMapper<PropertiesEntryDiff> ENTRY_ROW_MAPPER = new RowMapper<PropertiesEntryDiff>() {
        @Override
        public PropertiesEntryDiff mapRow(ResultSet rs, int i) throws SQLException {
            return new PropertiesEntryDiff(
                    rs.getLong("id"),
                    rs.getString("entry_key"),
                    rs.getString("group_id"),
                    rs.getString("profile"),
                    rs.getString("data_id"),
                    rs.getLong("version"),
                    rs.getString("value"),
                    rs.getLong("last_version"),
                    rs.getString("last_value"),
                    rs.getString("comment"),
                    PropertiesEntryDiff.EntryDiffType.codeOf(rs.getInt("type")),
                    rs.getString("operator"),
                    new Date(rs.getTimestamp("create_time").getTime())
            );
        }
    };

    private static final RowMapper<Long> ID_ROW_MAPPER = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong("id");
        }
    };

    private static final ResultSetExtractor<Integer> COUNT_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("total_count");
            }
            return null;
        }
    };
}