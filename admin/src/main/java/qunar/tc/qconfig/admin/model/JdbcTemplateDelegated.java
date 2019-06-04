package qunar.tc.qconfig.admin.model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dongcao on 2018/7/6.
 */
public class JdbcTemplateDelegated {

    private Map<DbEnv, JdbcTemplate> jdbcTemplates;

    public JdbcTemplateDelegated(Map<DbEnv, JdbcTemplate> jdbcTemplates) {
        this.jdbcTemplates = jdbcTemplates;
    }


    public int update(String sql, Object... args) throws DataAccessException {
        int n = 0;
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            n += jdbcTemplate.update(sql, args);
        }

        return n;
    }

    public void batchUpdate(String sql, BatchPreparedStatementSetter params) throws DataAccessException {
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            jdbcTemplate.batchUpdate(sql, params);
        }
    }

    public void batchUpdate(String sql, List<Object[]> batchArgs) {
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    public static <T> int batchUpdate(String sql, Map<JdbcTemplateDelegated, List<T>> batchMapping, Function<T, Object[]> function) {
        int result = 0;
        for (Map.Entry<JdbcTemplateDelegated, List<T>> entry : batchMapping.entrySet()) {
            result += entry.getKey().batchUpdate(sql, entry.getValue(), function);
        }
        return result;
    }

    public <T> int batchUpdate(String sql, List<T> entities, Function<T, Object[]> function) {
        int result = 0;
        List<Object[]> paramList = Lists.newLinkedList();
        for (T entity : entities) {
            Object[] params = function.apply(entity);
            paramList.add(params);
        }
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            int[] resultArray = jdbcTemplate.batchUpdate(sql, paramList);
            for (int count : resultArray) {
                result += count;
            }
        }
        return result;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        List<T> results = Lists.newArrayList();
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            results.addAll(jdbcTemplate.query(sql, rowMapper, args));
        }

        return results;
    }

    public <T> T query(String sql, ResultSetExtractor<T> extractor, Object...args) throws DataAccessException {
        T result = null;
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            result = jdbcTemplate.query(sql, extractor, args);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
        T result = null;
        for (JdbcTemplate jdbcTemplate : jdbcTemplates.values()) {
            result = jdbcTemplate.queryForObject(sql, requiredType, args);
            if (result != null) {
                break;
            }
        }

        return result;
    }

    public <T> Map<DbEnv, List<T>> queryOutDbEnv(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        Map<DbEnv, List<T>> results = Maps.newHashMapWithExpectedSize(jdbcTemplates.size());
        for (Map.Entry<DbEnv, JdbcTemplate> entry : jdbcTemplates.entrySet()) {
            List<T> lst = entry.getValue().query(sql, rowMapper, args);
            results.put(entry.getKey(), lst);
        }

        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JdbcTemplateDelegated that = (JdbcTemplateDelegated) o;
        if (jdbcTemplates != null) {
            if (that.jdbcTemplates == null) {
                return false;
            } else {
                Set<DbEnv> thisDbEnvSet = this.jdbcTemplates.keySet();
                Set<DbEnv> thatDbEnvSet = that.jdbcTemplates.keySet();
                return (thisDbEnvSet.containsAll(thatDbEnvSet) && thatDbEnvSet.containsAll(thisDbEnvSet));
            }
        } else {
            return that.jdbcTemplates == null;
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (jdbcTemplates == null) {
            return 0;
        } else {
            for (Map.Entry<DbEnv, JdbcTemplate> entry : jdbcTemplates.entrySet()) {
                hashCode = hashCode + entry.getKey().hashCode();
            }
        }
        return hashCode;
    }
}
