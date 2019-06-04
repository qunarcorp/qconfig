package qunar.tc.qconfig.admin.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.BatchPushTaskDao;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseState;
import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.admin.greyrelease.TaskConfig;
import qunar.tc.qconfig.admin.support.PaginationUtil;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class BatchPushTaskDaoImpl implements BatchPushTaskDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(BatchPushTaskDaoImpl.class);

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final static String SELECT_BY_UUID = "SELECT uuid, task_info, status," +
            " finished_batch_num, last_push_time, lock_version, create_time, update_time, operator FROM batch_push_task_new WHERE uuid=?";

    private final static String SELECT_BY_UUIDS = "SELECT uuid, task_info, status, " +
            "finished_batch_num, last_push_time, lock_version, create_time, update_time, operator FROM batch_push_task_new WHERE uuid IN ";

    private final static String INSERT_TASK =  "INSERT INTO batch_push_task_new(uuid, group_id, profile, lock_version, task_info," +
            " status, finished_batch_num, last_push_time, operator) VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?)";

    private final static String UPDATE_TASK = "UPDATE batch_push_task_new SET status=?, finished_batch_num=?, last_push_time=CURRENT_TIMESTAMP, lock_version=? WHERE uuid=?";

    private final static String UPDATE_TASK_WHERE_STATUS_NOT_IN = "UPDATE batch_push_task_new SET status=?, finished_batch_num=?, last_push_time=CURRENT_TIMESTAMP, lock_version=? WHERE uuid=? AND status!=? AND status!=?";

    private final static String SELECT_HISTORY_BY_META = "SELECT uuid, group_id, profile, task_info, status, " +
            " finished_batch_num, last_push_time, lock_version, create_time, update_time, operator FROM batch_push_task_new " +
            " WHERE group_id=? AND profile=? AND status IN(?,?) ORDER BY id DESC LIMIT ?,?";


    @Override
    public StatusInfo queryTask(String uuid) {
        return jdbcTemplate.query(SELECT_BY_UUID, TASK_EXTRACTOR, uuid);
    }

    @Override
    public boolean insertTask(StatusInfo status) {
        String taskConfigStr;
        try {
            taskConfigStr = MAPPER.writeValueAsString(status.getTaskConfig());
        } catch (JsonProcessingException e) {
            LOGGER.error("serializing grey release task config error, statusInfo:[{}]", status, e);
            throw new RuntimeException("serializing grey release task config error:" + e.getMessage());
        }
        ConfigMeta configMeta = status.getTaskConfig().getMetaVersions().get(0).getConfigMeta();
        return jdbcTemplate.update(INSERT_TASK, status.getUuid(), configMeta.getGroup(), configMeta.getProfile(), status.getLockVersion(), taskConfigStr, status.getState().getCode(),
                status.getFinishedBatchNum(), status.getOperator()) > 0;
    }

    @Override
    public boolean updateTaskStatus(StatusInfo status) {
        return jdbcTemplate.update(UPDATE_TASK, status.getState().getCode(), status.getFinishedBatchNum(),
                status.getLockVersion(), status.getUuid()) > 0;
    }

    @Override
    public boolean updateTaskStatus(StatusInfo status, GreyReleaseState notStatus1, GreyReleaseState notStatus2) {
        return jdbcTemplate.update(UPDATE_TASK_WHERE_STATUS_NOT_IN, status.getState().getCode(), status.getFinishedBatchNum(),
                status.getLockVersion(), status.getUuid(), notStatus1.getCode(), notStatus2.getCode()) > 0;
    }

    @Override
    public List<StatusInfo> selectUuidIn(List<String> uuids) {
        return jdbcTemplate.query(SELECT_BY_UUIDS + SQLUtil.generateStubs(uuids.size()), STATUS_INFO_MAPPER, uuids.toArray());
    }

    @Override
    public List<StatusInfo> queryHistoryTasks(ConfigMeta meta, long currentPage, long pageSize) {
        long offset = PaginationUtil.start(currentPage, pageSize);
        return jdbcTemplate.query(SELECT_HISTORY_BY_META, STATUS_INFO_MAPPER, meta.getGroup(),  meta.getProfile(),
                GreyReleaseState.FINISH.getCode(), GreyReleaseState.CANCEL.getCode(), offset, pageSize);
    }

    private static final RowMapper<StatusInfo> STATUS_INFO_MAPPER = new RowMapper<StatusInfo>() {
        @Override
        public StatusInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            return buildStatusInfo(rs);
        }
    };

    private static ResultSetExtractor<StatusInfo> TASK_EXTRACTOR = new ResultSetExtractor<StatusInfo>() {
        @Override
        public StatusInfo extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return buildStatusInfo(rs);
            } else {
                return null;
            }
        }
    };

    private static StatusInfo buildStatusInfo(ResultSet rs) throws SQLException {
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setUuid(rs.getString("uuid"));
        String taskConfigText = rs.getString("task_info");
        statusInfo.setState(GreyReleaseState.codeOf(rs.getInt("status")));
        statusInfo.setFinishedBatchNum(rs.getInt("finished_batch_num"));
        statusInfo.setLockVersion(rs.getInt("lock_version"));
        statusInfo.setCreateTime(new Date(rs.getTimestamp("create_time").getTime()));
        statusInfo.setUpdateTime(new Date(rs.getTimestamp("update_time").getTime()));
        statusInfo.setLastPushTime(new Date(rs.getTimestamp("last_push_time").getTime()));
        statusInfo.setOperator(rs.getString("operator"));
        try {
            TaskConfig taskConfig = MAPPER.readValue(taskConfigText, TaskConfig.class);
            statusInfo.setTaskConfig(taskConfig);
        } catch (IOException e) {
            LOGGER.error("parse task config error", e);
            throw new RuntimeException("parse task config error, " + e.getMessage());
        }
        return statusInfo;
    }
}
