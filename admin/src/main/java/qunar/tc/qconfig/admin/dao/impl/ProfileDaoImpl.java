package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ProfileDao;
import qunar.tc.qconfig.admin.support.SQLUtil;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 22:24
 */
@Repository
public class ProfileDaoImpl implements ProfileDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void create(String group, String profile, String operator) {
        jdbcTemplate.update(CREATE_SQL, group, profile, operator);
    }

    @Override
    public void batchCreate(final String group, final List<String> profiles, final String operator) {
        jdbcTemplate.batchUpdate(CREATE_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String profile = profiles.get(i);
                ps.setString(1, group);
                ps.setString(2, profile);
                ps.setString(3, operator);
            }

            @Override
            public int getBatchSize() {
                return profiles.size();
            }
        });
    }

    private static final String CREATE_SQL = "INSERT INTO config_profile(group_id,profile,operator) VALUES(?,?,?)";

    private static final String SELECT_PROFILES_IN_GROUPS_SQL = "SELECT group_id,profile FROM config_profile WHERE group_id IN ";

    @Override
    public List<String> selectProfiles(String group) {
        return jdbcTemplate.queryForList(SELECT_PROFILES_SQL, String.class, group);
    }

    @Override
    public List<Map.Entry<String, String>> selectProfiles(Collection<String> groups) {
        if (groups.isEmpty()) {
            return Lists.newArrayList();
        }

        return jdbcTemplate.query(SELECT_PROFILES_IN_GROUPS_SQL + SQLUtil.generateStubs(groups.size()), ENTRY_MAPPER, groups.toArray());
    }

    private static final String SELECT_PROFILES_SQL = "select profile from config_profile where group_id=?";


    private static final ResultSetExtractor<Integer> COUNT_MAPPER = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("count");
            } else {
                return 0;
            }
        }
    };

    @Override
    public boolean exist(String group, String profile) {
        return jdbcTemplate.query(EXIST_SQL, new Object[] {group, profile}, COUNT_MAPPER) != 0;
    }

    @Override
    public int completeDelete(String group, String profile) {
        return jdbcTemplate.update("DELETE FROM config_profile WHERE group_id=? AND profile=?", group, profile);
    }

    private static final String EXIST_SQL = "select count(*) count from config_profile where group_id=? and profile=?";

    private static final RowMapper<Map.Entry<String, String>> ENTRY_MAPPER = new RowMapper<Map.Entry<String, String>>() {
        @Override
        public Map.Entry<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Maps.immutableEntry(rs.getString("group_id"), rs.getString("profile"));
        }
    };
}
