package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.PermissionDao;
import qunar.tc.qconfig.admin.model.FilePermissionInfo;
import qunar.tc.qconfig.admin.model.PermissionInfo;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 11:57
 */
@Repository
public class PermissionDaoImpl implements PermissionDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String SELECT_PERMISSION_SQL = "select permission from permission where group_id = ? and rtx_id = ?";

    @Override
    public Integer selectPermission(String group, String rtxId) {
        return jdbcTemplate.query(SELECT_PERMISSION_SQL, new ResultSetExtractor<Integer>() {
            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return rs.getInt("permission");
                } else {
                    return null;
                }
            }
        }, group, rtxId);
    }

    private static final String SELECT_BY_GROUP = "select group_id, rtx_id, permission, update_time from permission where group_id = ?";

    @Override
    public List<PermissionInfo> selectPermissionsByGroup(String group) {
        return jdbcTemplate.query(SELECT_BY_GROUP, permissionMapper, group);
    }

    private static final String INSERT_OR_UPDATE = "insert into permission(group_id, rtx_id, permission, update_time)" +
            " values(?, ?, ?, ?) on duplicate key update permission = ?, update_time = ?";

    @Override
    public void batchInsertOrUpdatePermission(final List<PermissionInfo> permissionInfos) {
        List<Object[]> params = Lists.newLinkedList();
        for (PermissionInfo permissionInfo : permissionInfos) {
            Object[] param = {
                    permissionInfo.getGroup(),
                    permissionInfo.getRtxId(),
                    permissionInfo.getPermission(),
                    permissionInfo.getUpdateTime(),
                    permissionInfo.getPermission(),
                    permissionInfo.getUpdateTime()
            };
            params.add(param);
        }
        jdbcTemplate.batchUpdate(INSERT_OR_UPDATE, params);
    }

    private static final String SELECT_BY_RTX_ID = "select group_id, rtx_id, permission, update_time from permission where rtx_id = ?";

    @Override
    public List<PermissionInfo> selectPermissionsByRtxId(String rtxId) {
        return jdbcTemplate.query(SELECT_BY_RTX_ID, permissionMapper, rtxId);
    }

    @Override
    public List<FilePermissionInfo> selectFilePermissionsByGroupAndDataId(String group, String dataId) {
        return jdbcTemplate.query("SELECT group_id, data_id, rtx_id, permission, update_time FROM file_permission WHERE group_id=? AND data_id=?", filePermissionMapper, group, dataId);
    }

    @Override
    public List<FilePermissionInfo> selectFilePermissionsByRtxId(String rtxId) {
        return jdbcTemplate.query("SELECT group_id, data_id, rtx_id, permission, update_time FROM file_permission WHERE rtx_id = ?", filePermissionMapper, rtxId);
    }

    @Override
    public void batchInsertOrUpdateFilePermission(final List<FilePermissionInfo> permissionInfos) {
        List<Object[]> params = Lists.newLinkedList();
        String sql = "insert into file_permission(group_id, data_id, rtx_id, permission, update_time) values(?,?,?,?,?) on duplicate key update permission=?, update_time=?";
        for (FilePermissionInfo permissionInfo : permissionInfos) {
            Object[] param = {
                    permissionInfo.getGroup(),
                    permissionInfo.getDataId(),
                    permissionInfo.getRtxId(),
                    permissionInfo.getPermission(),
                    permissionInfo.getUpdateTime(),
                    permissionInfo.getPermission(),
                    permissionInfo.getUpdateTime()
            };
            params.add(param);
        }

        jdbcTemplate.batchUpdate(sql, params);
    }

    @Override
    public int deleteFilePermission(String group, String dataId, String rtxId) {
        return jdbcTemplate.update("DELETE FROM file_permission WHERE rtx_id=? AND group_id=? AND data_id=?", rtxId, group, dataId);
    }

    @Override
    public List<FilePermissionInfo> selectFilePermissionsByGroupAndRtxId(String group, String rtxId) {
        return jdbcTemplate.query("SELECT group_id, data_id, rtx_id, permission, update_time FROM file_permission WHERE rtx_id=? AND group_id=?", filePermissionMapper, rtxId, group);
    }

    private final RowMapper<PermissionInfo> permissionMapper = new RowMapper<PermissionInfo>() {
        @Override
        public PermissionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PermissionInfo permissionInfo = new PermissionInfo();
            permissionInfo.setGroup(rs.getString("group_id"));
            permissionInfo.setRtxId(rs.getString("rtx_id"));
            permissionInfo.setPermission(rs.getInt("permission"));
            permissionInfo.setUpdateTime(rs.getTimestamp("update_time"));
            return permissionInfo;
        }
    };

    private final RowMapper<FilePermissionInfo> filePermissionMapper = new RowMapper<FilePermissionInfo>() {
        @Override
        public FilePermissionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            FilePermissionInfo permissionInfo = new FilePermissionInfo();
            permissionInfo.setGroup(rs.getString("group_id"));
            permissionInfo.setDataId(rs.getString("data_id"));
            permissionInfo.setRtxId(rs.getString("rtx_id"));
            permissionInfo.setPermission(rs.getInt("permission"));
            permissionInfo.setUpdateTime(rs.getTimestamp("update_time"));
            return permissionInfo;
        }
    };
}
