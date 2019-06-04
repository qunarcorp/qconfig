package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.GroupOpLogDao;
import qunar.tc.qconfig.admin.model.GroupOpLog;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 21:26
 */
@Repository
public class GroupOpLogDaoImpl implements GroupOpLogDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${permissionLog.showLength}")
    private int showLength;

    private static final String INSERT_SQL = "insert into group_op_log(group_id, operator, remarks, operation_time) values(?, ?, ?, ?)";

    @Override
    public int insert(GroupOpLog groupOpLog) {
        return jdbcTemplate.update(INSERT_SQL, groupOpLog.getGroup(), groupOpLog.getOperator(),
                groupOpLog.getRemarks(), groupOpLog.getOpTime());
    }

    private static final String SELECT_BY_GROUP = "select group_id, operator, remarks, operation_time from group_op_log " +
            "where group_id = ? order by operation_Time desc limit ?, ?";

    @Override
    public List<GroupOpLog> selectRecentByGroup(String group) {
        return jdbcTemplate.query(SELECT_BY_GROUP, LOG_MAPPER, group, 0, showLength);
    }

    private static final RowMapper<GroupOpLog> LOG_MAPPER = new RowMapper<GroupOpLog>() {
        @Override
        public GroupOpLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new GroupOpLog(rs.getString("group_id"), rs.getString("operator"), rs.getString("remarks"),
                    rs.getTimestamp("operation_time"));
        }
    };
}
