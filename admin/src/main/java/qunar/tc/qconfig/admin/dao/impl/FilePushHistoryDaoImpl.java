package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FilePushHistoryDao;
import qunar.tc.qconfig.admin.model.FilePushHistory;
import qunar.tc.qconfig.admin.web.bean.PushStatus;
import qunar.tc.qconfig.admin.web.bean.PushType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pingyang.yang on 2018/10/23
 */
@Repository
public class FilePushHistoryDaoImpl implements FilePushHistoryDao {

    //新建语句
    private static final String INSERT_SQL = "INSERT INTO file_push_history(group_id, profile, data_id, version, md5 , ip, port, status, type, operator) values(?, ? , ?, ?, ?, INET_ATON(?), ?, ? , ?, ?)";
    //升级状态
    private static final String UPDATE_STATUS_SQL = "UPDATE file_push_history SET status = ? WHERE group_id = ? AND profile = ? AND data_id = ? AND version = ? ";
    private static final String UPDATE_STATUS_WITH_IP_SQL = "UPDATE file_push_history SET status = ? WHERE group_id = ? AND profile = ? AND data_id = ? AND version = ? AND ip = INET_ATON(?)";
    //查询文件版本大于某版本的推送历史
    private static final String SELECT_BY_VERSIONS_SQL = "SELECT group_id, profile, data_id, version, md5, status, type, inet_ntoa(ip) AS ip, port FROM file_push_history WHERE group_id = ? AND profile = ? and data_id = ? AND version > ? AND type = ?";
    private static final String DELETE_FILE_SQL = "DELETE FROM file_push_history WHERE group_id=? AND data_id=? AND profile=?";


    @Resource
    JdbcTemplate jdbcTemplate;

    @Override
    public int insert(FilePushHistory filePushHistory) {
        return jdbcTemplate.update(INSERT_SQL, filePushHistory.getGroup(), filePushHistory.getProfile(),
                filePushHistory.getDataId(), filePushHistory.getVersion(), filePushHistory.getMD5(),
                filePushHistory.getIP(), filePushHistory.getPort(), filePushHistory.getStatus().code(),
                filePushHistory.getType().code(), filePushHistory.getOperator());
    }


    @Override
    public void batchInsert(final List<FilePushHistory> filePushHistories) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FilePushHistory filePushHistory = filePushHistories.get(i);
                ps.setString(1, filePushHistory.getGroup());
                ps.setString(2, filePushHistory.getProfile());
                ps.setString(3, filePushHistory.getDataId());
                ps.setInt(4, filePushHistory.getVersion());
                ps.setString(5, filePushHistory.getMD5());
                ps.setString(6, filePushHistory.getIP());
                ps.setInt(7, filePushHistory.getPort());
                ps.setInt(8, filePushHistory.getStatus().code());
                ps.setInt(9, filePushHistory.getType().code());
                ps.setString(10, filePushHistory.getOperator());
            }

            @Override
            public int getBatchSize() {
                return filePushHistories.size();
            }
        });
    }

    @Override
    public int updateStatus(String group, String profile, String dataId, int version, PushStatus status) {
        return jdbcTemplate.update(UPDATE_STATUS_SQL, status.code(), group, profile, dataId, version);
    }

    @Override
    public int updateStatus(String group, String profile, String dataId, int version, String ip, PushStatus status) {
        return jdbcTemplate.update(UPDATE_STATUS_WITH_IP_SQL, status.code(), group, profile, dataId, version, ip);
    }

    @Override
    public List<FilePushHistory> getEditPushHistory(String group, String profile, String dataId, int publishVersion) {
        return jdbcTemplate.query(SELECT_BY_VERSIONS_SQL, HISTORY_MAPPER, group, profile, dataId, publishVersion,
                PushType.EDIT_PUSH.code());
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update(DELETE_FILE_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final RowMapper<FilePushHistory> HISTORY_MAPPER = new RowMapper<FilePushHistory>() {
        @Override
        public FilePushHistory mapRow(ResultSet rs, int i) throws SQLException {
            FilePushHistory history = new FilePushHistory();
            history.setDataId(rs.getString("data_id"));
            history.setGroup(rs.getString("group_id"));
            history.setProfile(rs.getString("profile"));
            history.setStatus(PushStatus.codeOf(rs.getInt("status")));
            history.setType(PushType.codeOf(rs.getInt("type")));
            history.setMD5(rs.getString("md5"));
            history.setVersion(rs.getInt("version"));
            history.setIP(rs.getString("ip"));
            history.setPort(rs.getInt("port"));
            return history;
        }
    };


}
