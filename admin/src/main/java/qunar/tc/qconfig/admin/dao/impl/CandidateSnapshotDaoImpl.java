package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.model.DbOpType;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.admin.model.VersionDetail;
import qunar.tc.qconfig.admin.support.IntArrayUtil;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/23 20:42
 */
@Repository
public class CandidateSnapshotDaoImpl implements CandidateSnapshotDao {

    private static final String INSERT_SQL = "insert into config_candidate_snapshot(group_id,data_id,profile," +
            "based_version,edit_version,content,status,operator,create_time) values(?,?,?,?,?,?,?,?,now())";

    private static final String INSERT_BETA_SQL = "insert into config_candidate_snapshot(group_id,data_id,profile," +
            "based_version,edit_version,content,status,operator,create_time) values(?,?,?,?,?,?,?,?,?)";

    private static final String FIND_SQL = "SELECT group_id,data_id,profile,based_version,edit_version,content," +
            "status,operator FROM config_candidate_snapshot WHERE group_id=? AND profile=? AND data_id=? " +
            "AND edit_version=?";

    private static final String SELECT_VERSION_AND_OPERATOR = "SELECT group_id,data_id,profile,edit_version,operator,create_time,status  FROM config_candidate_snapshot WHERE group_id = :group_id AND data_id = :data_id AND profile = :profile AND edit_version in (:versions)";

    private static final String SELECT_VERSION_AFTER_OPERATOR = "SELECT group_id,data_id,profile,edit_version,operator,create_time,status  FROM config_candidate_snapshot WHERE group_id = :group_id AND data_id = :data_id AND profile = :profile AND edit_version >= :version";

    private static final String FIND_WITHAPP_SQL = "SELECT group_id,data_id,profile, operator, create_time FROM config_candidate_snapshot WHERE create_time > :time AND group_id in (:groups) and status = 3";

    private static final String FIND_OPERATOR_SQL = "SELECT operator FROM config_candidate_snapshot " +
            "WHERE group_id=? AND profile=? AND data_id=? AND edit_version=?";

    private static final String FIND_VERSION_SQL = "SELECT edit_version FROM config_candidate_snapshot " +
            "WHERE group_id=? AND profile=? AND data_id=? AND status=?";

    private static final String FIND_VERSION_DETAIL_SQL = "SELECT edit_version, operator,create_time " +
            "FROM config_candidate_snapshot WHERE group_id=? AND profile=? AND data_id=? AND status=?";

    private static final String FIND_EDIT_VERSION_SQL = "SELECT edit_version FROM config_candidate_snapshot " +
            "WHERE group_id=? AND profile=? AND data_id=? AND edit_version=?";

    private static final String FIND_BY_STATUS_SQL = "SELECT group_id,data_id,profile,based_version,edit_version," +
            "content,status,operator, create_time FROM config_candidate_snapshot " +
            "WHERE group_id=? AND profile=? AND data_id=? AND status=? ORDER BY create_time desc limit ?";

    private static final String FIND_LAST_PUBLISHED_SQL = "SELECT group_id,data_id,profile,based_version," +
            "edit_version,content,status,operator FROM config_candidate_snapshot " +
            "WHERE group_id=? AND profile=? AND data_id=? AND edit_version<? AND status=? ORDER BY edit_version DESC LIMIT 1";

    private static final String DELETE_SQL = "DELETE FROM config_candidate_snapshot " +
            "WHERE group_id=? AND data_id=? AND profile=?";

    private static final String FIND_LATEST_SQL = "SELECT group_id, data_id, profile, based_version, " +
            "edit_version, content, status, operator FROM config_candidate_snapshot " +
            "WHERE group_id= ? AND profile= ? AND data_id= ? ORDER BY edit_version DESC LIMIT 1";

    private static final String SELECT_RANGE = "SELECT id, group_id, data_id, profile, based_version, " +
            "edit_version, content, status, operator, create_time FROM config_candidate_snapshot " +
            "WHERE id BETWEEN ? AND ? ORDER BY id ASC";

    // 注意这里id<max_id不取等号，否则会block下一条数据(max_id+1)的插入
    private static final String SELECT_ID_WITH_LOCK = "SELECT id from config_candidate_snapshot WHERE id>=? AND " +
            "id<(SELECT MAX(id) from config_candidate_snapshot) ORDER BY id ASC LIMIT ? FOR UPDATE";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void save(CandidateSnapshot snapshot) {
        jdbcTemplate.update(INSERT_SQL, snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile(),
                snapshot.getBasedVersion(), snapshot.getEditVersion(), snapshot.getData(),
                snapshot.getStatus().code(), snapshot.getOperator());
    }

    @Override
    // 用作beta数据同步
    public void saveBeta(CandidateSnapshot snapshot) {
        jdbcTemplate.update(INSERT_BETA_SQL, snapshot.getGroup(), snapshot.getDataId(),
                snapshot.getProfile(), snapshot.getBasedVersion(), snapshot.getEditVersion(), snapshot.getData(),
                snapshot.getStatus().code(), snapshot.getOperator(), snapshot.getUpdateTime());
    }

    @Override
    public int batchSave(List<CandidateSnapshot> candidateSnapshots) {
        List<Object[]> params = Lists.newLinkedList();
        for (CandidateSnapshot snapshot : candidateSnapshots) {
            Object[] param = {
                    snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile(),
                    snapshot.getBasedVersion(), snapshot.getEditVersion(), snapshot.getData(),
                    snapshot.getStatus().code(), snapshot.getOperator()
            };
            params.add(param);
        }
        return IntArrayUtil.getSum(jdbcTemplate.batchUpdate(INSERT_SQL, params));
    }

    @Override
    public CandidateSnapshot find(String group, String dataId, String profile, long editVersion) {
        return jdbcTemplate.query(FIND_SQL, SNAPSHOT_EXTRACTOR, group, profile, dataId, editVersion);
    }

    @Override
    public String findOperator(String group, String dataId, String profile, long version) {
        return jdbcTemplate.queryForObject(FIND_OPERATOR_SQL, OPERATOR_MAPPER, group, profile, dataId, version);
    }


    @Override
    public List<Long> findVersionsWithStatus(ConfigMeta configMeta, StatusType status) {
        return jdbcTemplate.queryForList(FIND_VERSION_SQL, Long.class,
                configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), status.code());
    }

    @Override
    public List<VersionDetail> findVersionsDetailWithStatus(ConfigMeta configMeta, StatusType status) {
        return jdbcTemplate.query(FIND_VERSION_DETAIL_SQL, VERSION_DETAIL_MAPPER,
                configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), status.code());
    }

    @Override
    public Long findEditVersion(ConfigMeta configMeta, long editVersion) {
        return jdbcTemplate.query(FIND_EDIT_VERSION_SQL,
                new Object[]{configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), editVersion},
                new ResultSetExtractor<Long>() {
                    @Override
                    public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return rs.getLong(rs.getInt("edit_version"));
                        }
                        return -1L;
                    }
                });
    }

    @Override
    public List<CandidateSnapshot> findCandidateSnapshots(ConfigMeta configMeta, StatusType status, int limit) {
        return jdbcTemplate.query(FIND_BY_STATUS_SQL, SNAPSHOT_MAPPER,
                configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), status.code(), limit);
    }

    @Override
    public CandidateSnapshot findLastPublish(ConfigMeta configMeta, long version) {
        return jdbcTemplate.query(FIND_LAST_PUBLISHED_SQL, SNAPSHOT_EXTRACTOR, configMeta.getGroup(),
                configMeta.getProfile(), configMeta.getDataId(), version, StatusType.PUBLISH.code());
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update(DELETE_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public CandidateSnapshot findLatestCandidateSnapshot(ConfigMeta configMeta) {
        return jdbcTemplate.query(FIND_LATEST_SQL, SNAPSHOT_EXTRACTOR, configMeta.getGroup(),
                configMeta.getProfile(), configMeta.getDataId());
    }

    @Override
    public List<CandidateSnapshot> findPublishedCandidateSnapshotsWithApps(List<String> groups, Date time) {

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        Map<String, Object> param = Maps.newHashMap();
        param.put("groups", groups);
        param.put("time", time);

        return template.query(FIND_WITHAPP_SQL, param, COUNT_SNAPSHOT_MAPPER);
    }

    @Override
    public boolean exist(String group, String dataId, int version) {
        return jdbcTemplate.query("SELECT id FROM config_candidate_snapshot WHERE group_id=? AND data_id=? AND edit_version=? limit 1", ID_EXTRACTOR, group, dataId, version) != null;
    }

    @Override
    public List<VersionDetail> findVersionsDetailWithBegin(ConfigMeta configMeta, int begin) {
        return jdbcTemplate.query("SELECT edit_version, operator,create_time FROM config_candidate_snapshot " +
                        "WHERE group_id=? AND profile=? AND data_id=? AND edit_version>?", VERSION_DETAIL_MAPPER,
                configMeta.getGroup(), configMeta.getProfile(), configMeta.getDataId(), begin);
    }

    @Override
    public List<CandidateSnapshot> getSnapshotInVersion(ConfigMeta meta, Set<Long> versions) {
        if (versions.size() == 0) {
            return Lists.newArrayList();
        }
        NamedParameterJdbcTemplate nameJdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("group_id", meta.getGroup());
        parameter.addValue("data_id", meta.getDataId());
        parameter.addValue("profile", meta.getProfile());
        parameter.addValue("versions", versions);
        return nameJdbc.query(SELECT_VERSION_AND_OPERATOR, parameter, SNAPSHOT_MAPPER);
    }

    @Override
    public List<CandidateSnapshot> getSnapshotAfterVersion(ConfigMeta meta, Long version) {
        if (version < 0) {
            return Lists.newArrayList();
        }
        NamedParameterJdbcTemplate nameJdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("group_id", meta.getGroup());
        parameter.addValue("version", version);
        parameter.addValue("profile", meta.getProfile());
        parameter.addValue("data_id", meta.getDataId());
        return nameJdbc.query(SELECT_VERSION_AFTER_OPERATOR, parameter, SNAPSHOT_MAPPER);
    }

    @Override
    public List<CandidateSnapshot> scanSnapshots(long begin, long end) {
        if (begin > end) return ImmutableList.of();
        return jdbcTemplate.query(SELECT_RANGE, SNAPSHOT_ALL_MAPPER, begin, end);
    }

    @Override
    public List<Long> scanIdsWithLock(long begin, long limit) {
        return jdbcTemplate.query(SELECT_ID_WITH_LOCK, ID_MAPPER, begin, limit);
    }

    @Override
    public long findLatestRecordId() {
        return jdbcTemplate.query("SELECT id FROM config_candidate_snapshot ORDER BY id DESC LIMIT 1", new ResultSetExtractor<Long>() {
            @Override
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                return null;
            }
        });
    }

    private static final RowMapper<String> OPERATOR_MAPPER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString("operator");
        }
    };

    private static final RowMapper<CandidateSnapshot> SNAPSHOT_MAPPER = new RowMapper<CandidateSnapshot>() {
        @Override
        public CandidateSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CandidateSnapshot(
                    rs.getString("group_id"),
                    rs.getString("data_id"),
                    rs.getString("profile"),
                    rs.getString("operator"),
                    rs.getLong("edit_version"),
                    new Date(rs.getTimestamp("create_time").getTime()),
                    StatusType.codeOf(rs.getInt("status")));
        }
    };


    private static final RowMapper<CandidateSnapshot> COUNT_SNAPSHOT_MAPPER = new RowMapper<CandidateSnapshot>() {
        @Override
        public CandidateSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CandidateSnapshot(
                    rs.getString("group_id"),
                    rs.getString("data_id"),
                    rs.getString("profile"),
                    rs.getString("operator"),
                    new Date(rs.getTimestamp("create_time").getTime()));
        }
    };



    private static ResultSetExtractor<CandidateSnapshot> SNAPSHOT_EXTRACTOR = new ResultSetExtractor<CandidateSnapshot>() {
        @Override
        public CandidateSnapshot extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new CandidateSnapshot(new Candidate(rs.getString("group_id"), rs.getString("data_id"),
                        rs.getString("profile"), rs.getLong("based_version"), rs.getLong("edit_version"),
                        StatusType.codeOf(rs.getInt("status"))), rs.getString("content"), rs.getString("operator"));
            } else {
                return null;
            }
        }
    };

    private static final RowMapper<VersionDetail> VERSION_DETAIL_MAPPER = new RowMapper<VersionDetail>() {
        @Override
        public VersionDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new VersionDetail(rs.getLong("edit_version"), rs.getString("operator"),
                    rs.getTimestamp("create_time"));
        }
    };

    private static final RowMapper<Long> ID_MAPPER = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getLong("id");
        }
    };

    private static final ResultSetExtractor<Integer> ID_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return null;
            }
        }
    };

    private static RowMapper<CandidateSnapshot> SNAPSHOT_ALL_MAPPER = new RowMapper<CandidateSnapshot>() {
        @Override
        public CandidateSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CandidateSnapshot(
                    rs.getLong("id"),
                    rs.getString("group_id"),
                    rs.getString("data_id"),
                    rs.getString("profile"),
                    rs.getLong("based_version"),
                    rs.getLong("edit_version"),
                    rs.getString("content"),
                    rs.getString("operator"),
                    StatusType.codeOf(rs.getInt("status")),
                    new Date(rs.getTimestamp("create_time").getTime())
                   );
        }
    };

}
