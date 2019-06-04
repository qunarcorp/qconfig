package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.model.*;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ReferenceStatus;
import qunar.tc.qconfig.common.bean.StatusType;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * changed by chenjinkai
 * <p>
 * 复用config_reference相关表，增加type字段用以区分引用还是继承。
 * type 0 为引用，type 1为继承
 * <p>
 * Date: 14-6-30 Time: 下午2:59
 *
 * @author: xiao.liang
 * @description:
 */
@Repository("referenceDao")
public class ReferenceDaoImpl implements ReferenceDao {

    @Resource
    protected JdbcTemplate jdbcTemplate;

    @Resource
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final int DELETE_STATUS_CODE = ReferenceStatus.DELETE.code();

    private static final String INSERT_SQL = "INSERT INTO config_reference(group_id,alias,profile,ref_group_id,ref_data_id,ref_profile,operator, type) VALUES(?,?,?,?,?,?,?,?)";

    private static final String INSERT_BETA = "INSERT INTO config_reference(group_id,alias,profile,ref_group_id,ref_data_id,ref_profile,operator,type,create_time) VALUES(?,?,?,?,?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE ref_group_id=?,ref_data_id=?,ref_profile=?,operator=?,type=?,create_time=?";

    private static final String SCAN_SQL = "SELECT id, group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operator,type,create_time FROM config_reference WHERE id>=? ORDER BY id ASC limit ?";

    private static final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE = "SELECT A.group_id,A.profile,A.alias as data_id,A.ref_group_id,A.ref_profile,A.ref_data_id,A.status,A.operator,B.version,B.public_status,B.update_time " +
            "FROM config_reference A INNER JOIN config B ON A.group_id=? AND A.profile=? AND A.status!="
            + DELETE_STATUS_CODE + " AND A.ref_group_id = B.group_id AND A.ref_data_id = B.data_id AND A.ref_profile = B.profile "
            + "where A.type = ?";

    private static final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILEPAGE = "SELECT A.group_id,A.profile,A.alias as data_id,A.ref_group_id,A.ref_profile,A.ref_data_id,A.status,A.operator,B.version,B.public_status,B.update_time " +
            "FROM config_reference A INNER JOIN config B ON A.group_id=? AND A.profile=? AND A.status!="
            + DELETE_STATUS_CODE + " AND A.ref_group_id = B.group_id AND A.ref_data_id = B.data_id AND A.ref_profile = B.profile "
            + "where A.type = ?" + " ORDER BY (A.create_time) DESC limit ?, ?";

    private static final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_KEYWORD = "SELECT A.group_id,A.profile,A.alias as data_id,A.ref_group_id,A.ref_profile,A.ref_data_id,A.status,A.operator,B.version,B.public_status,B.update_time " +
            "FROM config_reference A INNER JOIN config B ON A.group_id=? AND A.profile=? AND A.status!="
            + DELETE_STATUS_CODE + " AND A.alias LIKE ? AND A.ref_group_id = B.group_id AND A.ref_data_id = B.data_id AND A.ref_profile = B.profile"
            + " where A.type = ?";


    private static final String DELETE_SQL = "UPDATE config_reference SET status=" + ReferenceStatus.DELETE.code()
            + " WHERE group_id=? AND profile=? AND alias=? AND ref_group_id=? AND ref_data_id=? AND ref_profile=?";

    private static final String UPDATE_STATUS_FROM_REF_META_SQL = "UPDATE config_reference SET status=? WHERE ref_group_id=? AND ref_data_id=? AND ref_profile=? and type = ?";

    private static final String REFERENCE_COUNT_SQL = "SELECT COUNT(*) count FROM config_reference WHERE  ref_group_id=? AND ref_data_id=? AND ref_profile=? AND status!=" + ReferenceStatus.DELETE.code() + " and type = ?";

    private static final String FIND_BY_REFERENCE_SQL = "SELECT group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operator,create_time " +
            "FROM config_reference " +
            "WHERE ref_group_id=? AND ref_data_id=? AND ref_profile=? AND status != ? and type = ? limit 1";

    private static final String FIND_META_IN_GROUPS_SQL = "SELECT ref_group_id,ref_profile,ref_data_id FROM config_reference " +
            "WHERE `group_id` IN (:groups) AND `status`!=:status and type=:type";

    private static final String FIND_REFERENCE_IN_REFGROUPS_SQL = "SELECT group_id,profile,alias,ref_group_id,ref_data_id," +
            "ref_profile,operator,create_time FROM config_reference " +
            "WHERE `ref_group_id` IN (:refGroups) AND `status`!=:status and type=:type";

    private static final String FIND_REFERENCE_IN_GROUPS_SQL = "SELECT group_id,profile,alias,ref_group_id,ref_data_id," +
            "ref_profile,operator,create_time FROM config_reference " +
            "WHERE `group_id` IN (:groups) AND `status`!=:status and type=:type";

    private static final String FIND_REFERENCE_IN_GROUPS_LIKE_PROFILE_SQL = "SELECT group_id,profile,alias,ref_group_id," +
            "ref_data_id,ref_profile,operator,create_time FROM config_reference " +
            "WHERE `group_id` IN (:groups) AND `profile` LIKE :profile AND `status`!=:status and type=:type";

    private static final String FIND_SNAPSHOT_BY_GROUP = "SELECT a.group_id AS group_id,a.alias AS data_id," +
            "a.profile AS profile,c.based_version AS based_version,c.version AS edit_version,c.content AS content," +
            "a.ref_group_id AS ref_group_id,a.ref_data_id AS ref_data_id,a.ref_profile AS ref_profile," +
            "b.update_time AS update_time " +
            "FROM (config_reference AS a INNER JOIN config AS b ON a.group_id=? AND a.ref_group_id=b.group_id " +
            "AND a.ref_data_id=b.data_id AND a.ref_profile=b.profile and type = ?) " +
            "INNER JOIN config_snapshot AS c ON b.group_id=c.group_id AND b.data_id=c.data_id " +
            "AND b.profile=c.profile AND b.version=c.version";

    @Override
    public int create(Reference reference) {
        return jdbcTemplate.update(INSERT_SQL, reference.getGroup(), reference.getAlias(), reference.getProfile(),
                reference.getRefGroup(), reference.getRefDataId(), reference.getRefProfile(), reference.getOperator(),
                reference.getType());
    }

    @Override
    public int insertBeta(Reference reference) {
        return jdbcTemplate.update(INSERT_BETA, reference.getGroup(), reference.getAlias(), reference.getProfile(),
                reference.getRefGroup(), reference.getRefDataId(), reference.getRefProfile(), reference.getOperator(),
                reference.getType(), reference.getCreateTime(),reference.getRefGroup(), reference.getRefDataId(),
                reference.getRefProfile(), reference.getOperator(), reference.getType(), reference.getCreateTime());
    }

    @Override
    public List<Reference> scanReference(long start, long limit) {
        return jdbcTemplate.query(SCAN_SQL, REFERENCE_ALL_MAPPER, start, limit);
    }

    @Override
    public List<Reference> findEverReferences(String group, String profile, int type) {
        return jdbcTemplate
                .query("SELECT group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operator,create_time, `type` FROM config_reference WHERE group_id=? AND profile=? AND type=?",
                        REFERENCE_MAPPER, group, profile, type);
    }

    @Override
    public List<Reference> findEverReferences(String group, String profile) {
        return findEverReferences(group, profile, RefType.REFERENCE.value());
    }

    @Override
    public Reference findEverReference(ConfigMeta meta, int type) {
        return jdbcTemplate.query("SELECT group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operator,create_time, `type` FROM config_reference WHERE group_id=? and profile=? AND alias=? AND type = ?",
                REFERENCE_EXTRACTOR, meta.getGroup(), meta.getProfile(), meta.getDataId(), type);
    }

    @Override
    public Reference findEverReference(ConfigMeta meta) {
        return findEverReference(meta, RefType.REFERENCE.value());
    }

    @Override
    public ConfigMeta findReference(ConfigMeta meta) {
        return findReference(meta, RefType.REFERENCE.value());
    }

    @Override
    public ConfigMeta findReference(ConfigMeta meta, int type) {
        return jdbcTemplate
                .query("SELECT ref_group_id,ref_profile,ref_data_id FROM config_reference WHERE group_id=? AND profile=? AND alias=? AND status!=? and type = ?",
                        META_EXTRACTOR, meta.getGroup(), meta.getProfile(), meta.getDataId(), DELETE_STATUS_CODE, type);
    }

    @Override
    public List<ConfigMeta> findReferences(String group) {
        MapSqlParameterSource params = createParameterByGroupsOrRefGroups(Sets.newHashSet(group), false);
        return namedParameterJdbcTemplate.query(FIND_META_IN_GROUPS_SQL, params, REF_META_MAPPER);
    }

    @Override
    public List<Reference> findReferenceInfos(String group) {
        MapSqlParameterSource params = createParameterByGroupsOrRefGroups(Sets.newHashSet(group), false);
        return namedParameterJdbcTemplate.query(FIND_REFERENCE_IN_GROUPS_SQL, params, REFERENCE_MAPPER);
    }

    @Override
    public Reference findByReferenced(ConfigMeta refMeta) {
        return findByReferenced(refMeta, RefType.REFERENCE.value());
    }

    @Override
    public List<Reference> findByReferences(Set<String> refGroups) {
        MapSqlParameterSource params = createParameterByGroupsOrRefGroups(refGroups, true);
        return namedParameterJdbcTemplate.query(FIND_REFERENCE_IN_REFGROUPS_SQL, params, REFERENCE_MAPPER);
    }

    private MapSqlParameterSource createParameterByGroupsOrRefGroups(Set<String> groups, boolean isRef) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (isRef) {
            params.addValue("refGroups", groups);
        } else {
            params.addValue("groups", groups);
        }
        params.addValue("status", DELETE_STATUS_CODE);
        params.addValue("type", RefType.REFERENCE.value());

        return params;
    }

    @Override
    public List<Reference> findReferences(String group, String profile) {
        return jdbcTemplate.query("SELECT group_id,profile,alias,ref_group_id,ref_data_id,ref_profile,operator,create_time FROM config_reference WHERE group_id=? AND profile=?",
                REFERENCE_MAPPER, group, profile);
    }

    @Override
    public Reference findByReferenced(ConfigMeta refMeta, int type) {
        return jdbcTemplate.query(FIND_BY_REFERENCE_SQL, REFERENCE_EXTRACTOR, refMeta.getGroup(),
                refMeta.getDataId(), refMeta.getProfile(), ReferenceStatus.DELETE.code(), type);
    }

    @Override
    public List<Reference> searchReferences(Set<String> groups, String profile, int type) {
        final MapSqlParameterSource parameters = createParameterByGroupsOrRefGroups(groups, false);
        parameters.addValue("type", type);
        if (Strings.isNullOrEmpty(profile)) {
            return namedParameterJdbcTemplate.query(FIND_REFERENCE_IN_GROUPS_SQL, parameters, REFERENCE_MAPPER);
        }

        parameters.addValue("profile", profile + "%");
        return namedParameterJdbcTemplate.query(FIND_REFERENCE_IN_GROUPS_LIKE_PROFILE_SQL, parameters, REFERENCE_MAPPER);
    }

    @Override
    public List<Reference> searchReferences(Set<String> groups, String profile) {
        return searchReferences(groups, profile, RefType.REFERENCE.value());
    }

    private static final ResultSetExtractor<ConfigMeta> META_EXTRACTOR = new ResultSetExtractor<ConfigMeta>() {
        @Override
        public ConfigMeta extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new ConfigMeta(rs.getString("ref_group_id"), rs.getString("ref_data_id"),
                        rs.getString("ref_profile"));
            }
            return null;
        }
    };

    public List<PublishedConfigInfo> findReferenceDetail(String group, String profile) {
        return jdbcTemplate.query(FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE,
                CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER, group, profile, RefType.REFERENCE.value());
    }

    public List<PublishedConfigInfo> findReferenceDetailPage(String group, String profile, int start, int pageSize) {
        return jdbcTemplate.query(FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILEPAGE,
                CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER, group, profile, RefType.REFERENCE.value(), start, pageSize);
    }

    @Override
    public List<PublishedConfigInfo> findReferenceDetail(String group, String profile, String keyword) {
        keyword = "%" + SQLUtil.escapeWildcards(keyword) + "%";
        final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_KEYWORD = FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE + " AND A.alias LIKE ?";
        return jdbcTemplate.query(FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_KEYWORD,
                CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER, group, profile, RefType.REFERENCE.value(), keyword);
    }


    @Override
    public List<PublishedConfigInfo> findReferenceDetailPage(String group, String profile, String keyword, int start, int pageSize) {
        keyword = "%" + SQLUtil.escapeWildcards(keyword) + "%";
        final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_KEYWORD = FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE + " AND A.alias LIKE ?" + " ORDER BY (A.create_time) DESC limit ?, ?";
        return jdbcTemplate.query(FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_KEYWORD,
                CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER, group, profile, RefType.REFERENCE.value(), keyword, start, pageSize);
    }

    @Override
    public List<PublishedConfigInfo> findReferenceDetailByMeta(String group, String profile, String dataId) {
        final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_DATA_ID = FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE + " AND A.alias=?";
        return jdbcTemplate.query(FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_DATA_ID,
                CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER, group, profile, RefType.REFERENCE.value(), dataId);
    }

    @Override
    public List<PublishedConfigInfo> findReferenceDetailByMetaPage(String group, String profile, String dataId, int start, int pageSize) {
        final String FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_DATA_ID = FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE + " AND A.alias=?" + " ORDER BY (A.create_time) DESC limit ?, ?";
        return jdbcTemplate.query(FIND_REFERENCE_DETAIL_BY_GROUP_AND_PROFILE_AND_DATA_ID,
                CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER, group, profile, RefType.REFERENCE.value(), dataId, start, pageSize);
    }

    @Override
    public int delete(Reference reference) {
        return jdbcTemplate.update(DELETE_SQL, reference.getGroup(), reference.getProfile(), reference.getAlias(),
                reference.getRefGroup(), reference.getRefDataId(), reference.getRefProfile());
    }

    //todo
    @Override
    public int updateStatusFromRefMeta(ConfigMeta refMeta, ReferenceStatus referenceStatus) {
        return updateStatusFromRefMeta(refMeta, referenceStatus, RefType.REFERENCE.value());
    }

    @Override
    public int updateStatusFromRefMeta(ConfigMeta refMeta, ReferenceStatus referenceStatus, int type) {
        return jdbcTemplate.update(UPDATE_STATUS_FROM_REF_META_SQL, referenceStatus.code(), refMeta.getGroup(),
                refMeta.getDataId(), refMeta.getProfile(), type);
    }

    @Override
    public int referenceCount(ConfigMeta refMeta) {
        return referenceCount(refMeta, RefType.REFERENCE.value());
    }

    private static final ResultSetExtractor<Integer> COUNT_MAPPER = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("count");
            } else {
                return 0;
            }
        }
    };

    @Override
    public int referenceCount(ConfigMeta refMeta, int type) {
        return jdbcTemplate.query(REFERENCE_COUNT_SQL,
                new Object[]{refMeta.getGroup(), refMeta.getDataId(), refMeta.getProfile(), type}, COUNT_MAPPER);
    }

    @Override
    public int completeDelete(ConfigMeta meta) {

        return jdbcTemplate.update("DELETE FROM config_reference WHERE group_id=? AND profile=? AND alias=?",
                meta.getGroup(), meta.getProfile(), meta.getDataId());
    }

    @Override
    public List<CandidateSnapshot> findCurrentSnapshotsInGroup(String group, int type) {
        List<Map.Entry<CandidateSnapshot, ConfigMeta>> result = findCurrentSnapshotsWithSourceInGroup(group, type);
        if (result == null) {
            return ImmutableList.of();
        }
        List<CandidateSnapshot> snapshots = Lists.newArrayListWithCapacity(result.size());
        for (Map.Entry<CandidateSnapshot, ConfigMeta> entry : result) {
            snapshots.add(entry.getKey());
        }
        return snapshots;
    }

    @Override
    public List<Map.Entry<CandidateSnapshot, ConfigMeta>> findCurrentSnapshotsWithSourceInGroup(String group) {
        return findCurrentSnapshotsWithSourceInGroup(group, RefType.REFERENCE.value());
    }

    @Override
    public List<CandidateSnapshot> findCurrentSnapshotsInGroup(String group) {
        return findCurrentSnapshotsInGroup(group, RefType.REFERENCE.value());
    }

    @Override
    public List<Map.Entry<CandidateSnapshot, ConfigMeta>> findCurrentSnapshotsWithSourceInGroup(String group, int type) {
        return jdbcTemplate.query(FIND_SNAPSHOT_BY_GROUP,
                SNAPSHOT_WITH_SOURCE_MAPPER, group, type);
    }

    private static final RowMapper<PublishedConfigInfo> CANDIDATE_WITH_PUBLIC_STATUS_AND_REFERENCE_MAPPER = new RowMapper<PublishedConfigInfo>() {
        @Override
        public PublishedConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PublishedConfigInfo configInfo = new PublishedConfigInfo();
            configInfo.setConfigMeta(new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile")));
            configInfo.setRefConfigMeta(new ConfigMeta(rs.getString("ref_group_id"), rs.getString("ref_data_id"), rs.getString("ref_profile")));
            configInfo.setPublish(true);//引用类型只有发布状态
            configInfo.setUpdateTime(rs.getTimestamp("update_time"));
            configInfo.setRefType(RefType.REFERENCE);
            configInfo.setVersion(rs.getLong("version")); //注意这里rs.getLong方法的定义，当值为null时返回0，虽然重构了，但是这个还是留着吧
            configInfo.setOperator(rs.getString("operator"));
            configInfo.setStatusType(StatusType.PUBLISH);
            return configInfo;
        }
    };

    private static final RowMapper<Reference> REFERENCE_MAPPER = new RowMapper<Reference>() {
        @Override
        public Reference mapRow(ResultSet rs, int rowNum) throws SQLException {
            return createReference(rs);
        }
    };

    private static final RowMapper<Reference> REFERENCE_ALL_MAPPER = new RowMapper<Reference>() {
        @Override
        public Reference mapRow(ResultSet rs, int rowNum) throws SQLException {
            Reference reference = createReference(rs);
            reference.setId(rs.getLong("id"));
            reference.setType(rs.getInt("type"));
            return reference;
        }
    };

    private static final ResultSetExtractor<Reference> REFERENCE_EXTRACTOR = new ResultSetExtractor<Reference>() {
        @Override
        public Reference extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return createReference(rs);
            }
            return null;
        }
    };

    private static Reference createReference(ResultSet rs) throws SQLException {
        return new Reference(rs.getString("group_id"), rs.getString("profile"), rs.getString("alias"),
                rs.getString("ref_group_id"), rs.getString("ref_profile"), rs.getString("ref_data_id"),
                rs.getString("operator"), rs.getTimestamp("create_time"));
    }

    private static final RowMapper<Map.Entry<CandidateSnapshot, ConfigMeta>> SNAPSHOT_WITH_SOURCE_MAPPER = new RowMapper<Map.Entry<CandidateSnapshot, ConfigMeta>>() {
        @Override
        public Map.Entry<CandidateSnapshot, ConfigMeta> mapRow(ResultSet rs, int rowNum) throws SQLException {
            CandidateSnapshot snapshot = new CandidateSnapshot(new Candidate(rs.getString("group_id"), rs.getString("data_id"),
                    rs.getString("profile"), rs.getLong("based_version"), rs.getLong("edit_version"),
                    StatusType.PUBLISH, rs.getTimestamp("update_time")), rs.getString("content"), "");
            ConfigMeta source = new ConfigMeta(rs.getString("ref_group_id"), rs.getString("ref_data_id"), rs.getString("ref_profile"));
            return Maps.immutableEntry(snapshot, source);
        }
    };

    private static final RowMapper<ConfigMeta> REF_META_MAPPER = new RowMapper<ConfigMeta>() {
        @Override
        public ConfigMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConfigMeta(
                    rs.getString("ref_group_id"),
                    rs.getString("ref_data_id"),
                    rs.getString("ref_profile"));
        }
    };
}
