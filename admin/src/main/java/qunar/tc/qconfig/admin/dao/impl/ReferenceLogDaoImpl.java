package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ReferenceLogDao;
import qunar.tc.qconfig.admin.model.ReferenceLog;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Date: 14-7-7 Time: 下午4:52
 * 
 * @author: xiao.liang
 * @description:
 */
@Repository
public class ReferenceLogDaoImpl implements ReferenceLogDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO config_reference_log(group_id,alias,profile,ref_group_id,ref_data_id,ref_profile,operator,operation_type) VALUES(?,?,?,?,?,?,?,?)";

    private static final String FIND_BY_CONFIG_META_SQL = "SELECT group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operation_type,operator,create_time FROM config_reference_log WHERE ref_group_id=? AND ref_profile=? AND ref_data_id=? AND operation_type = ? order by create_time desc";

    @Override
    public int create(ReferenceLog referenceLog) {
        return jdbcTemplate.update(INSERT_SQL, referenceLog.getGroup(), referenceLog.getAlias(),
                referenceLog.getProfile(), referenceLog.getRefGroup(), referenceLog.getRefDataId(),
                referenceLog.getRefProfile(), referenceLog.getOperator(), referenceLog.getChangeType().code());
    }

    @Override
    public List<ReferenceLog> find(ConfigMeta configMeta, RefType refType) {
        RefChangeType refChangeType = RefChangeType.ADD;
        if(refType == RefType.INHERIT) {
            refChangeType = RefChangeType.INHERIT;
        }

        return jdbcTemplate.query(FIND_BY_CONFIG_META_SQL, REFERENCE_LOG_MAPPER, configMeta.getGroup(),
                configMeta.getProfile(), configMeta.getDataId(), refChangeType.code());
    }

    @Override
    public List<ReferenceLog> selectRecent(String group, String profile, int length) {
        return jdbcTemplate.query("SELECT group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operation_type,operator,create_time FROM config_reference_log WHERE group_id=? AND profile=? ORDER BY create_time DESC limit ?, ?",
                REFERENCE_LOG_MAPPER,group, profile, 0, length);
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update("DELETE FROM config_reference_log WHERE group_id=? AND profile=? AND alias=?", meta.getGroup(), meta.getProfile(), meta.getDataId());
    }

    private static final RowMapper<ReferenceLog> REFERENCE_LOG_MAPPER = new RowMapper<ReferenceLog>() {
        @Override
        public ReferenceLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReferenceLog(rs.getString("group_id"), rs.getString("profile"), rs.getString("alias"),
                    rs.getString("ref_group_id"), rs.getString("ref_profile"), rs.getString("ref_data_id"),
                    rs.getString("operator"), RefChangeType.codeOf(rs.getInt("operation_type")),
                    rs.getTimestamp("create_time"));
        }
    };
}
