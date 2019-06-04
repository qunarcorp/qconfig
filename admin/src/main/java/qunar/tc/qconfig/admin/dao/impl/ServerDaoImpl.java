package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ServerDao;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by dongcao on 2018/7/2.
 */
@Repository
public class ServerDaoImpl implements ServerDao {

    private static final String SELECT_SQL = "SELECT INET_NTOA(ip) ip from server";

    private static final String DELETE_SQL = "delete from server where INET_NTOA(ip) = ?";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<String> getServers() {
        return jdbcTemplate.query(SELECT_SQL, SERVER_MAPPER);
    }

    @Override
    public int deleteServer(String ip) {
        return jdbcTemplate.update(DELETE_SQL, ip);
    }

    private static final RowMapper<String> SERVER_MAPPER = new RowMapper<String>() {

        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("ip");
        }
    };

}
