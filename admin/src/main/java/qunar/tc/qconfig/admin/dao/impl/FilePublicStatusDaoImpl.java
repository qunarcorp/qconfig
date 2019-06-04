package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FilePublicStatusDao;
import qunar.tc.qconfig.admin.model.FilePublicRecord;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:49
 */
@Repository
public class FilePublicStatusDaoImpl implements FilePublicStatusDao {

    private static final String INSERT_SQL = "INSERT INTO  file_public_status(group_id, data_id, type, create_time) " +
            "VALUES(?, ?, ?, now()) ON DUPLICATE KEY UPDATE update_time=now(), type = ?";

    private static final String INSERT_BETA = "INSERT INTO `file_public_status`(group_id, data_id, `type`, create_time, update_time) " +
            "VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `type`=?, update_time=?";

    private static final String SCAN_SQL = "SELECT * FROM file_public_status WHERE update_time>? OR id>=? ORDER BY update_time ASC LIMIT ?";

    private static final String DELETE_SQL = "delete from file_public_status where group_id=? and data_id=?";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public PublicType getPublicType(ConfigMetaWithoutProfile meta) {
        return jdbcTemplate.query("SELECT type FROM file_public_status WHERE group_id = ? AND data_id = ?",
                PUBLICTYPE_EXTRACTOR, meta.getGroup(), meta.getDataId());
    }

    @Override
    public boolean exist(ConfigMetaWithoutProfile meta) {
        return exist(meta, PublicType.REFERENCE_MASK);
    }

    @Override
    public boolean exist(ConfigMetaWithoutProfile meta, int mask) {
        return jdbcTemplate.query("SELECT id FROM file_public_status WHERE group_id=? AND data_id=? " +
                "AND (type & ?) = ?", ID_EXTRACTOR, meta.getGroup(), meta.getDataId(), mask, mask) != null;
    }

    @Override
    public int insert(ConfigMetaWithoutProfile meta, int value) {
        return jdbcTemplate.update(INSERT_SQL, meta.getGroup(), meta.getDataId(), value, value);
    }

    @Override
    public int insertOrUpdateBeta(ConfigMetaWithoutProfile meta, int type, Timestamp updateTime) {
        return jdbcTemplate.update(INSERT_BETA, meta.getGroup(), meta.getDataId(), type, updateTime, updateTime, type, updateTime);
    }

    @Override
    public List<FilePublicRecord> scan(Timestamp startTime, long startId, long limit) {
        return jdbcTemplate.query(SCAN_SQL, FILE_PUBLIC_RECORD_ROW_MAPPER, startTime, startId, limit);
    }

    @Override
    public int batchSetPublic(List<PublicConfigInfo> publicConfigInfos) {
        List<Object[]> paramList = Lists.newLinkedList();
        for (PublicConfigInfo publicConfigInfo : publicConfigInfos) {
            Object[] params = new Object[]{
                    publicConfigInfo.getConfigMetaWithoutProfile().getGroup(),
                    publicConfigInfo.getConfigMetaWithoutProfile().getDataId(),
                    publicConfigInfo.getPublicType().code(),
                    publicConfigInfo.getPublicType().code()
            };
            paramList.add(params);
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, paramList);
        return publicConfigInfos.size();
    }

    @Override
    public List<String> selectDataIds(String group, int mask) {
        return jdbcTemplate.query("SELECT data_id FROM file_public_status WHERE group_id = ? " +
                "and (type & ?) = ?", DATA_ID_MAPPER, group, mask, mask);
    }

    @Override
    public List<String> selectPublicDataIds(String group) {
        return jdbcTemplate.query("SELECT data_id, type FROM file_public_status WHERE group_id = ?",
                PUBLIC_DATA_ID_MAPPER, group);
    }

    @Override
    public void delete(String group, String dataId) {
        jdbcTemplate.update(DELETE_SQL, group, dataId);
    }

    @Override
    public List<String> selectDataIds(String group) {
        return selectDataIds(group, PublicType.REFERENCE_MASK);
    }

    public List<String> findAllPublicGroup(int mask){
        List<String> findAllPublicGroup = jdbcTemplate.queryForList("SELECT DISTINCT (group_id) FROM file_public_status WHERE (type & ?) = ?",
                String.class, mask, mask);
        return findAllPublicGroup;
    }

    private static final ResultSetExtractor<List<String>> PUBLIC_DATA_ID_MAPPER = new ResultSetExtractor<List<String>>() {
        @Override
        public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<String> result = new LinkedList<>();
            int type = rs.getInt("type");
            String dataId = rs.getString("data_id");
            PublicType publicType = new PublicType(type);
            if (publicType.isPublic()
                    && Strings.isNullOrEmpty(dataId)) {
                result.add(dataId);
            }
            return result;
        }

    };

    private static final RowMapper<String> DATA_ID_MAPPER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("data_id");
        }
    };

    private static final ResultSetExtractor<Integer> ID_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("id");
            }
            return null;
        }
    };

    private static final ResultSetExtractor<PublicType> PUBLICTYPE_EXTRACTOR = new ResultSetExtractor<PublicType>() {
        @Override
        public PublicType extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                int type = rs.getInt("type");
                return new PublicType(type);
            }
            return null;
        }
    };

    private static final RowMapper<FilePublicRecord> FILE_PUBLIC_RECORD_ROW_MAPPER = new RowMapper<FilePublicRecord>() {
        @Override
        public FilePublicRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new FilePublicRecord(rs.getLong("id"), rs.getString("group_id"),
                    rs.getString("data_id"), rs.getTimestamp("create_time"),
                    rs.getTimestamp("update_time"), rs.getInt("type"));
        }
    };
}
