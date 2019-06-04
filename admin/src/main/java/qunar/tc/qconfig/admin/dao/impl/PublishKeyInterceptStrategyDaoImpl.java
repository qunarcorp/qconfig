package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.PublishKeyInterceptStrategyDao;
import qunar.tc.qconfig.admin.model.InterceptStrategy;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhenyu.nie created on 2017 2017/3/22 20:39
 */
@Repository
public class PublishKeyInterceptStrategyDaoImpl implements PublishKeyInterceptStrategyDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public InterceptStrategy select(String group) {
        return jdbcTemplate.query("SELECT strategy FROM publish_key_intercept_strategy WHERE group_id=?", STRATEGY_EXTRACTOR, group);
    }

    @Override
    public int update(String group, InterceptStrategy strategy, String operator) {
        return jdbcTemplate.update("INSERT INTO publish_key_intercept_strategy(group_id, strategy, operator, create_time) VALUES(?, ?, ?, now()) ON DUPLICATE KEY UPDATE strategy=?, operator=?",
                group, strategy.code(), operator, strategy.code(), operator);
    }

    private static final ResultSetExtractor<InterceptStrategy> STRATEGY_EXTRACTOR = new ResultSetExtractor<InterceptStrategy>() {
        @Override
        public InterceptStrategy extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return InterceptStrategy.codeOf(resultSet.getInt("strategy"));
            }
            return null;
        }
    };
}
