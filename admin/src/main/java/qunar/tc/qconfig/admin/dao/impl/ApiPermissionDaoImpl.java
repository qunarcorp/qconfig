package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.model.ApiPermission;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by chenjk on 2018/1/12.
 */
@Repository
public class ApiPermissionDaoImpl {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public void save(ApiPermission apiPermission) {
        jdbcTemplate.update("insert into api_permission (url, parentid, method, type, description) values (?, ?, ?, ?, ?)"
                , apiPermission.getUrl(), apiPermission.getParentid(), apiPermission.getMethod(), apiPermission.getType(), apiPermission.getDescription());
    }

    public void delete(Long id) {
        jdbcTemplate.update("delete from api_permission where id = ?", id);
    }

    public List<ApiPermission> queryAll() {
        return jdbcTemplate.query("select * from api_permission order by datachange_lasttime desc", API_PERMISSION_MAPPER);
    }

    public static final RowMapper API_PERMISSION_MAPPER = new RowMapper<ApiPermission>() {
        @Override
        public ApiPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String url = rs.getString("url");
            Long parentId = rs.getLong("parentid");
            String method = rs.getString("method");
            int type = rs.getInt("type");
            String description = rs.getString("description");
            Timestamp datachangeLasttime = rs.getTimestamp("datachange_lasttime");
            return new ApiPermission(id, url, parentId, method, type, description, datachangeLasttime);
        }
    };
}
