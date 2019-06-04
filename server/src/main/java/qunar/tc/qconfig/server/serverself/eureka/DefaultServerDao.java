package qunar.tc.qconfig.server.serverself.eureka;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/8/10 17:06
 */
@Repository
public class DefaultServerDao implements ServerDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public int insert(QConfigServer server) {
        return jdbcTemplate.update("INSERT INTO server(ip, port, room) VALUES(INET_ATON(?),?,?) ON DUPLICATE KEY UPDATE room=?",
                server.getIp(), server.getPort(), server.getRoom(), server.getRoom());
    }

    @Override
    public String selectRoom(String ip) {
        return jdbcTemplate.query("SELECT room FROM server WHERE ip=INET_ATON(?)", ROOM_EXTRACTOR, ip);
    }

    @Override
    public List<QConfigServer> selectServers() {
        return jdbcTemplate.query("SELECT INET_NTOA(ip) as ip, port, room FROM server", SERVER_MAPPER);
    }

    private static final ResultSetExtractor<String> ROOM_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("room");
            }
            return null;
        }
    };

    private static final RowMapper<QConfigServer> SERVER_MAPPER = new RowMapper<QConfigServer>() {
        @Override
        public QConfigServer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new QConfigServer(rs.getString("ip"), rs.getInt("port"), rs.getString("room"));
        }
    };
}
