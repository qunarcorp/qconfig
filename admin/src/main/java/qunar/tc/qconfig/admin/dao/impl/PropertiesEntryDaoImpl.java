package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.PropertiesEntryDao;
import qunar.tc.qconfig.admin.model.PropertiesEntry;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author keli.wang
 */
@Repository
public class PropertiesEntryDaoImpl implements PropertiesEntryDao {
    private static final String INSERT_SQL =
            "INSERT INTO `properties_entries`(`key`,`group_id`,`profile`,`data_id`,`value`,`version`,`create_time`,`searchable`) VALUES(?,?,?,?,?,?,now(),1)";

    private static final String UPDATE_SQL =
            "UPDATE `properties_entries` SET `value`=?,`version`=?,`searchable`=1 " +
                    "WHERE `key`=? AND `group_id`=? AND `profile`=? AND `data_id`=? AND `version`=?";

    private static final String SELECT_BY_CONFIG_META_SQL =
            "SELECT `key`, `group_id`, `profile`, `data_id`, `version`, `value` " +
                    "FROM `properties_entries` " +
                    "WHERE `group_id`=? AND `profile`=? AND `data_id`=?";

    private static final String SELECT_BY_REF_SQL =
            "SELECT `key`, `group_id`, `profile`, `data_id`, `version`, `value` " +
                    "FROM `properties_entries` " +
                    "WHERE `key`=? AND `group_id`=? AND `profile`=? AND `data_id`=?";

    private static final String SELECT_SQL_TEMPLATE =
            "SELECT `key`, `group_id`, `profile`, `data_id`, `version`, `value` " +
                    "FROM `properties_entries` " +
                    "WHERE `key`=:key AND `group_id` IN (:groups) %s AND `searchable`=1 LIMIT :offset, :limit";

    private static final String SELECT_COUNT_SQL_TEMPLATE = "SELECT COUNT(*) AS `total_count` FROM `properties_entries` " +
            "WHERE `key`=:key AND `group_id` IN (:groups) %s AND `searchable`=1";

    private static final String DELETE_SQL =
            "UPDATE `properties_entries` SET `version`=?,`searchable`=0 " +
                    "WHERE `key`=? AND `group_id`=? AND `profile`=? AND `data_id`=? AND `version`=?";

    private static final RowMapper<PropertiesEntry> ENTRY_ROW_MAPPER =
            new RowMapper<PropertiesEntry>() {
                @Override
                public PropertiesEntry mapRow(ResultSet rs, int i) throws SQLException {
                    return new PropertiesEntry(rs.getString("key"),
                                               rs.getString("group_id"),
                                               rs.getString("profile"),
                                               rs.getString("data_id"),
                                               rs.getLong("version"),
                                               rs.getString("value"));
                }
            };

    private static final ResultSetExtractor<Integer> SELECT_COUNT_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("total_count");
            }
            return null;
        }
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void insert(final PropertiesEntry entry) {
        jdbcTemplate.update(INSERT_SQL,
                            entry.getKey(),
                            entry.getGroupId(),
                            entry.getProfile(),
                            entry.getDataId(),
                            entry.getValue(),
                            entry.getVersion());
    }

    @Override
    public int update(final PropertiesEntry entry, final long oldVersion) {
        return jdbcTemplate.update(UPDATE_SQL,
                                   entry.getValue(),
                                   entry.getVersion(),
                                   entry.getKey(),
                                   entry.getGroupId(),
                                   entry.getProfile(),
                                   entry.getDataId(),
                                   oldVersion);
    }

    @Override
    public List<PropertiesEntry> selectByConfigMeta(final ConfigMeta configMeta) {
        final List<PropertiesEntry> entries = jdbcTemplate.query(SELECT_BY_CONFIG_META_SQL,
                                                                 new Object[]{configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId()},
                                                                 ENTRY_ROW_MAPPER);
        if (entries == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(entries);
    }

    @Override
    public List<PropertiesEntry> selectByRef(String key, final Reference ref) {
        return jdbcTemplate.query(SELECT_BY_REF_SQL,
                                  new Object[]{key, ref.getRefGroup(), ref.getRefProfile(), ref.getRefDataId()},
                                  new RowMapper<PropertiesEntry>() {
                                      @Override
                                      public PropertiesEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                                          return new PropertiesEntry(rs.getString("key"),
                                                                     ref.getGroup(),
                                                                     ref.getProfile(),
                                                                     ref.getAlias(),
                                                                     rs.getLong("version"),
                                                                     rs.getString("value"));
                                      }
                                  });
    }

    @Override
    public List<PropertiesEntry> select(String key, Set<String> groups, String profile, int pageNo, int pageSize) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("key", key);
        params.addValue("groups", groups);

        final StringBuilder extraQueryParam = new StringBuilder();
        if (!Strings.isNullOrEmpty(profile)) {
            extraQueryParam.append(" AND `profile` like :profile ");
            params.addValue("profile", profile + "%");
        }

        // 页码从1开始
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 分页参数
        params.addValue("offset", (pageNo - 1) * pageSize);
        params.addValue("limit", pageSize);

        return namedParameterJdbcTemplate.query(String.format(SELECT_SQL_TEMPLATE, extraQueryParam),
                                                params,
                                                ENTRY_ROW_MAPPER);
    }

    @Override
    public int selectCount(String key, Set<String> groups, String profile) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("key", key);
        params.addValue("groups", groups);

        final StringBuilder extraQueryParam = new StringBuilder();
        if (!Strings.isNullOrEmpty(profile)) {
            extraQueryParam.append(" AND `profile` like :profile ");
            params.addValue("profile", profile + "%");

        }
        return namedParameterJdbcTemplate.query(String.format(SELECT_COUNT_SQL_TEMPLATE, extraQueryParam), params, SELECT_COUNT_EXTRACTOR);
    }

    @Override
    public int delete(final PropertiesEntry entry, final long newVersion) {
        return jdbcTemplate.update(DELETE_SQL,
                                   newVersion,
                                   entry.getKey(),
                                   entry.getGroupId(),
                                   entry.getProfile(),
                                   entry.getDataId(),
                                   entry.getVersion());
    }
}
