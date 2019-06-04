package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.model.DbOpType;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.admin.model.UnpublisedConfigInfo;
import qunar.tc.qconfig.admin.support.IntArrayUtil;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 17:40
 */
@Repository
public class CandidateDaoImpl implements CandidateDao {


    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO config_candidate(group_id,data_id,profile,based_version," +
            "edit_version,status,create_time) VALUES(?,?,?,?,?,?,now())";

    private static final String SELECT_SQL = "SELECT group_id,data_id,profile,based_version,edit_version,status,update_time " +
            "FROM config_candidate WHERE group_id=? AND profile=? AND data_id=?";

    private static final String FIND_IN_GROUP_SQL = "SELECT group_id,data_id,profile,based_version,edit_version," +
            "status,update_time FROM config_candidate WHERE group_id=? and status!=?";

    private static final String FIND_SQL = "SELECT group_id,data_id,profile,based_version,edit_version,status,update_time " +
            "FROM config_candidate WHERE group_id=? and profile=? and status in(%s)";

    private static final String SELECT_UNPUBLISHED = "SELECT a.group_id, a.data_id, a.profile, a.based_version, " +
            "a.edit_version, a.status, a.update_time, b.ref_group_id, b.ref_data_id, b.ref_profile, c.type, b.type reftype " +
            "FROM config_candidate a " +
            "left join config_reference b on a.group_id = b.group_id and a.data_id = b.alias and a.profile = b.profile " +
            "left join file_public_status c on a.group_id = c.group_id and a.data_id = c.data_id " +
            " WHERE a.group_id= ? and a.profile= ? and a.status in(%s)";


    private static final String SELECT_UNPUBLISHEDPAGE = "SELECT a.group_id, a.data_id, a.profile, a.based_version, " +
            "a.edit_version, a.status, a.update_time, b.ref_group_id, b.ref_data_id, b.ref_profile, c.type, b.type reftype " +
            "FROM config_candidate a " +
            "left join config_reference b on a.group_id = b.group_id and a.data_id = b.alias and a.profile = b.profile " +
            "left join file_public_status c on a.group_id = c.group_id and a.data_id = c.data_id " +
            " WHERE a.group_id= ? and a.profile= ? and a.status in(%s)" + " ORDER BY a.update_time DESC limit ?, ?";

    private static final String SELECT_UNPUBLISHED_WITH_KEYWORD = SELECT_UNPUBLISHED + " and a.data_id LIKE ?";

    private static final String SELECT_UNPUBLISHED_WITH_KEYWORDPAGE = SELECT_UNPUBLISHED + " and a.data_id LIKE ?" + " ORDER BY a.update_time DESC limit ?, ?";


    private static final String SELECT_UNPUBLISHED_WITH_DATAID = SELECT_UNPUBLISHED + " and a.data_id = ?";

    private static final String SELECT_UNPUBLISHED_WITH_DATAIDPAGE = SELECT_UNPUBLISHED + " and a.data_id = ?" + " ORDER BY a.update_time DESC limit ?, ?";

    private static final String DELETE_SQL = "DELETE FROM config_candidate WHERE group_id=? AND data_id=? AND profile=?";

    private static final String UPDATE_SQL = "UPDATE config_candidate SET based_version=?,edit_version=edit_version+1,status=? " +
            "WHERE group_id=? AND profile=? AND data_id=? AND edit_version=? AND status=?";

    private static final String BATCH_UPDATE_SQL = "UPDATE config_candidate SET based_version=?,edit_version=edit_version+1,status=? " +
            "WHERE group_id=? AND profile=? AND data_id=? AND edit_version=?";

    private static final String EXIST_IN_ENV_SQL = "SELECT count(*) as c FROM config_candidate " +
            "WHERE group_id=? AND data_id=? AND profile like ?";

    private static final String EXIST_SQL = "SELECT id FROM config_candidate WHERE group_id=? AND data_id=? limit 1";

    private static final String EXIST_NO_STATUS_SQL = "SELECT COUNT(*) as c FROM config_candidate " +
            "WHERE group_id=? AND profile=? AND data_id=? AND status!=?";

    private static final String FIND_IN_GROUP_PROFILE_SQL = "SELECT group_id,data_id,profile,based_version," +
            "edit_version,status,update_time FROM config_candidate WHERE group_id=? AND profile=?";

    private static final String FIND_NO_STATUS_SQL = "SELECT group_id,data_id,profile,based_version,edit_version," +
            "status,update_time FROM config_candidate WHERE group_id=? AND profile=? AND status!=?";


    private static final String INSERT_OR_UPDATE = "INSERT INTO config_candidate (group_id,data_id,profile, " +
            "based_version,edit_version,status,create_time,update_time) VALUES(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
            "based_version=?, edit_version=?, status=?, update_time=?";

    @Override
    public void save(Candidate candidate) {
        jdbcTemplate.update(INSERT_SQL, candidate.getGroup(), candidate.getDataId(), candidate.getProfile(),
                candidate.getBasedVersion(), candidate.getEditVersion(), candidate.getStatus().code());
    }

    @Override
    public int update(Candidate candidate, StatusType type) {
        return jdbcTemplate.update(UPDATE_SQL, candidate.getBasedVersion(), candidate.getStatus().code(), candidate.getGroup(),
                candidate.getProfile(), candidate.getDataId(), candidate.getEditVersion() - 1, type.code());
    }

    @Override
    public void insertOrUpdateBeta(Candidate candidate) {
        jdbcTemplate.update(INSERT_OR_UPDATE, candidate.getGroup(), candidate.getDataId(), candidate.getProfile(),
                candidate.getBasedVersion(), candidate.getEditVersion(), candidate.getStatus().code(), candidate.getUpdateTime(), candidate.getUpdateTime(),
                candidate.getBasedVersion(), candidate.getEditVersion(), candidate.getStatus().code(), candidate.getUpdateTime());
    }

    @Override
    public int batchUpdate(List<Candidate> candidateList) {
        List<Object[]> params = Lists.newLinkedList();
        for (Candidate candidate : candidateList) {
            Object[] param = {
                    candidate.getBasedVersion(), candidate.getStatus().code(), candidate.getGroup(),
                    candidate.getProfile(), candidate.getDataId(), candidate.getEditVersion() - 1
            };
            params.add(param);
        }
        return IntArrayUtil.getSum(jdbcTemplate.batchUpdate(BATCH_UPDATE_SQL, params));
    }

    @Override
    public int batchSave(List<Candidate> candidateList) {
        List<Object[]> params = Lists.newLinkedList();
        for (Candidate candidate : candidateList) {
            Object[] param = {
                    candidate.getGroup(), candidate.getDataId(),
                    candidate.getProfile(), candidate.getBasedVersion(),
                    candidate.getEditVersion(), candidate.getStatus().code()
            };
            params.add(param);
        }
       return IntArrayUtil.getSum(jdbcTemplate.batchUpdate(INSERT_SQL, params));
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update(DELETE_SQL, meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    @Override
    public Candidate find(String group, String dataId, String profile) {
        return jdbcTemplate.query(SELECT_SQL, CANDIDATE_EXTRACTOR, group, profile, dataId);
    }

    @Override
    public List<Candidate> find(String group, String profile) {
        return jdbcTemplate.query(FIND_IN_GROUP_PROFILE_SQL, CANDIDATE_MAPPER, group, profile);
    }

    @Override
    public List<Candidate> findWithoutStatus(String group, String profile, StatusType type) {
        return jdbcTemplate.query(FIND_NO_STATUS_SQL, CANDIDATE_MAPPER, group, profile, type.code());
    }

    @Override
    public boolean existInEnvironment(String group, String dataId, Environment environment) {
        return jdbcTemplate.query(EXIST_IN_ENV_SQL, COUNT_EXTRACTOR, group, dataId, environment.defaultProfile() + "%") != 0;
    }

    @Override
    public boolean exist(String group, String dataId) {
        return jdbcTemplate.query(EXIST_SQL, ID_EXTRACTOR, group, dataId) != null;
    }

    @Override
    public boolean existWithoutStatus(String group, String profile, String dataId, StatusType withoutType) {
        return jdbcTemplate.query(EXIST_NO_STATUS_SQL, COUNT_EXTRACTOR, group, profile, dataId, withoutType.code()) != 0;
    }

    @Override
    public List<Candidate> find(String group) {
        return jdbcTemplate.query(FIND_IN_GROUP_SQL, CANDIDATE_MAPPER, group, StatusType.DELETE.code());
    }

    /**
     * 使用group 和文件名称，找出符合的文件
     *
     * @param group 文件group
     * @param dataId 文件名称
     * @return 文件信息列表
     */
    @Override
    public List<Candidate> findByDataId(String group, String dataId) {
        return jdbcTemplate.query("SELECT group_id,data_id,profile,based_version,edit_version,status,update_time FROM config_candidate WHERE group_id=? AND data_id=?", CANDIDATE_MAPPER, group, dataId);
    }

    @Override
    public List<Candidate> find(String group, String profile, List<StatusType> statusTypes) {
        return select(group, profile, statusTypes, FIND_SQL, CANDIDATE_MAPPER);
    }

    @Override
    public List<UnpublisedConfigInfo> findUnpublished(String group, String profile, List<StatusType> statusTypes) {
        return select(group, profile, statusTypes, SELECT_UNPUBLISHED, UNPUBLISHED_CANDIDATE_MAPPER);
    }

    @Override
    public List<UnpublisedConfigInfo> findUnpublishedPage(String group, String profile, List<StatusType> statusTypes, int start, int pageSize) {
        return selectPage(group, profile, statusTypes,start, pageSize, SELECT_UNPUBLISHEDPAGE, UNPUBLISHED_CANDIDATE_MAPPER);
    }

    @Override
    public List<UnpublisedConfigInfo> findUnpublishedWithDataId(String group, String profile, String dataId, List<StatusType> statusTypes) {
        if (statusTypes.isEmpty()) {
            return Lists.newArrayList();
        }
        String sql = String.format(SELECT_UNPUBLISHED_WITH_DATAID, SQLUtil.generateQuestionMarks(statusTypes.size()));
        List<Object> params = buildSelectParams(group, profile, statusTypes);
        params.add(dataId);
        return jdbcTemplate.query(sql, UNPUBLISHED_CANDIDATE_MAPPER, params.toArray());
    }


    @Override
    public List<UnpublisedConfigInfo> findUnpublishedWithDataIdPage(String group, String profile, String dataId, List<StatusType> statusTypes, int start, int pageSize) {
        if (statusTypes.isEmpty()) {
            return Lists.newArrayList();
        }
        String sql = String.format(SELECT_UNPUBLISHED_WITH_DATAIDPAGE, SQLUtil.generateQuestionMarks(statusTypes.size()));
        List<Object> params = buildSelectParamsPage(group, profile, statusTypes);
        params.add(dataId);
        params.add(start);
        params.add(pageSize);
        return jdbcTemplate.query(sql, UNPUBLISHED_CANDIDATE_MAPPER, params.toArray());
    }

    public List<UnpublisedConfigInfo> findUnpublishedWithKeyword(String group, String profile, String keyword, List<StatusType> statusTypes) {
        if (statusTypes.isEmpty()) {
            return Lists.newArrayList();
        }
        String sql = String.format(SELECT_UNPUBLISHED_WITH_KEYWORD, SQLUtil.generateQuestionMarks(statusTypes.size()));
        List<Object> params = buildSelectParams(group, profile, statusTypes);
        params.add("%" + SQLUtil.escapeWildcards(keyword) + "%");
        return jdbcTemplate.query(sql, UNPUBLISHED_CANDIDATE_MAPPER, params.toArray());
    }


    public List<UnpublisedConfigInfo> findUnpublishedWithKeywordPage(String group, String profile, String keyword, List<StatusType> statusTypes, int start, int pageSize) {
        if (statusTypes.isEmpty()) {
            return Lists.newArrayList();
        }
        String sql = String.format(SELECT_UNPUBLISHED_WITH_KEYWORDPAGE, SQLUtil.generateQuestionMarks(statusTypes.size()));
        List<Object> params = buildSelectParamsPage(group, profile, statusTypes);
        params.add("%" + SQLUtil.escapeWildcards(keyword) + "%");
        params.add(start);
        params.add(pageSize);
        return jdbcTemplate.query(sql, UNPUBLISHED_CANDIDATE_MAPPER, params.toArray());
    }

    private <T> List<T> select(String group, String profile, List<StatusType> statusTypes, String format, RowMapper<T> mapper) {
        if (statusTypes == null || statusTypes.isEmpty()) {
            return Lists.newArrayList();
        }

        String sql = String.format(format, SQLUtil.generateQuestionMarks(statusTypes.size()));
        List<Object> params = buildSelectParams(group, profile, statusTypes);
        return jdbcTemplate.query(sql, mapper, params.toArray());
    }

    private <T> List<T> selectPage(String group, String profile,List<StatusType> statusTypes, int start, int pageSize, String format, RowMapper<T> mapper) {
        if (statusTypes == null || statusTypes.isEmpty()) {
            return Lists.newArrayList();
        }

        String sql = String.format(format, SQLUtil.generateQuestionMarks(statusTypes.size()));
        List<Object> params = buildSelectParamsPage(group, profile,start, pageSize, statusTypes);
        return jdbcTemplate.query(sql, mapper, params.toArray());
    }

    private List<Object> buildSelectParamsPage(String group, String profile, List<StatusType> statusTypes) {
        List<Object> params = Lists.newArrayListWithExpectedSize(statusTypes.size() + 4);
        params.add(group);
        params.add(profile);
        for (StatusType status : statusTypes) {
            params.add(status.code());
        }
        return params;
    }


    private List<Object> buildSelectParamsPage(String group, String profile,int start, int pageSize, List<StatusType> statusTypes) {
        List<Object> params = Lists.newArrayListWithExpectedSize(statusTypes.size() + 4);
        params.add(group);
        params.add(profile);
        for (StatusType status : statusTypes) {
            params.add(status.code());
        }
        params.add(start);
        params.add(pageSize);
        return params;
    }

    private List<Object> buildSelectParams(String group, String profile, List<StatusType> statusTypes) {
        List<Object> params = Lists.newArrayListWithExpectedSize(statusTypes.size() + 2);
        params.add(group);
        params.add(profile);
        for (StatusType status : statusTypes) {
            params.add(status.code());
        }
        return params;
    }

    private static final RowMapper<Candidate> CANDIDATE_MAPPER = new RowMapper<Candidate>() {
        @Override
        public Candidate mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Candidate(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"),
                    rs.getLong("based_version"), rs.getLong("edit_version"), StatusType.codeOf(rs.getInt("status")),
                    rs.getTimestamp("update_time"));
        }
    };

    private static final RowMapper<UnpublisedConfigInfo> UNPUBLISHED_CANDIDATE_MAPPER = new RowMapper<UnpublisedConfigInfo>() {
        @Override
        public UnpublisedConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            UnpublisedConfigInfo result = new UnpublisedConfigInfo();
            result.setConfigMeta(new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile")));
            result.setBasedVersion(rs.getLong("based_version"));
            result.setEditVersion(rs.getLong("edit_version"));
            result.setUpdateTime(rs.getTimestamp("update_time"));
            result.setStatusType(StatusType.codeOf(rs.getInt("status")));
            if (!Strings.isNullOrEmpty(rs.getString("ref_group_id"))
                    && !Strings.isNullOrEmpty(rs.getString("ref_data_id"))
                    && !Strings.isNullOrEmpty(rs.getString("ref_profile"))) {
                result.setRefConfigMeta(new ConfigMeta(rs.getString("ref_group_id"), rs.getString("ref_data_id"), rs.getString("ref_profile")));
                int reftype = rs.getInt("reftype");
                result.setRefType(RefType.codeOf(reftype));
            } else {//只有第一层文件才有publicType
                int type = rs.getInt("type");
                if (type != 0) {
                    result.setPublicType(new PublicType(type));
                }
            }
            return result;
        }
    };

    private static final ResultSetExtractor<Candidate> CANDIDATE_EXTRACTOR = new ResultSetExtractor<Candidate>() {
        @Override
        public Candidate extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new Candidate(rs.getString("group_id"),
                        rs.getString("data_id"),
                        rs.getString("profile"),
                        rs.getLong("based_version"),
                        rs.getLong("edit_version"),
                        StatusType.codeOf(rs.getInt("status")),
                        rs.getTimestamp("update_time"));
            }
            return null;
        }
    };

    private static final ResultSetExtractor<Integer> COUNT_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("c");
            } else {
                return 0;
            }
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
}
