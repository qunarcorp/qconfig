package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.model.*;
import qunar.tc.qconfig.admin.support.IntArrayUtil;
import qunar.tc.qconfig.admin.support.PaginationUtil;
import qunar.tc.qconfig.admin.support.SQLUtil;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PublicStatus;
import qunar.tc.qconfig.servercommon.bean.ReferenceStatus;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * User: zhaohuiyu Date: 5/13/14 Time: 2:41 PM
 */
@Repository
public class ConfigDaoImpl implements ConfigDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO config(group_id,data_id,profile,version,create_time) VALUES(?,?,?,?,?)";

    private static final String UPDATE_SQL = "UPDATE config SET version=?,public_status=? WHERE group_id=? AND data_id=? AND profile=? AND version=?";

    private static final String UPDATE_BETA = "UPDATE config SET version=?,public_status=?,update_time=? WHERE group_id=? AND data_id=? AND profile=? AND version=?";

    private static final String INSERT_OR_UPDATE_BETA = "INSERT INTO config(group_id,data_id,profile,version,public_status,create_time,update_time) VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE version=?,public_status=?,update_time=?";

    private static final String BATCH_UPDATE_SQL = "UPDATE config SET version=?,public_status=? WHERE group_id=? AND data_id=? AND profile=?";

    private static final String FIND_CONFIG_SQL = "SELECT group_id,profile,data_id,version,public_status,update_time FROM config WHERE group_id=? AND data_id=? AND profile=?";

    private static final String DELETE_SQL = "UPDATE config SET public_status=? WHERE group_id=? AND data_id=? AND profile=? AND version=?";

    private static final String FIND_PUBLIC_CONFIG_IN_PROFILES_AND_RESOURCES = "SELECT a.group_id AS group_id, a.profile AS profile, a.data_id AS data_id, a.version AS version, a.public_status AS public_status, a.update_time AS update_time FROM config AS a" +
            " INNER JOIN file_public_status AS b ON a.group_id=b.group_id AND a.data_id=b.data_id AND (a.profile like ? or a.profile=?) AND a.public_status!=? AND (b.type & ?) = ?";
    
    private static final String SELECT_PUBLIC_CONFIG_IN_PROFILES_AND_RESOURCE = "SELECT a.group_id AS group_id, a.profile AS profile, a.data_id AS data_id, a.version AS version, a.public_status AS public_status, a.update_time AS update_time FROM config AS a " +
            " INNER JOIN file_public_status AS b ON a.group_id=b.group_id AND a.data_id=b.data_id AND (a.profile LIKE :profile_like OR a.profile=:profile_res) AND a.public_status!=:status_del AND (b.type & :mask_ref)=:mask_ref AND a.group_id IN (:groups) %s LIMIT :offset, :limit";

    private static final String COUNT_PUBLIC_CONFIG_IN_PROFILES_AND_RESOURCE = "SELECT COUNT(*) AS total_count FROM config AS a " +
            " INNER JOIN file_public_status AS b ON a.group_id=b.group_id AND a.data_id=b.data_id AND (a.profile LIKE :profile_like OR a.profile=:profile_res) AND a.public_status!=:status_del AND (b.type & :mask_ref)=:mask_ref AND a.group_id IN (:groups) %s ";

    private static final String FIND_IN_ENVIRONMENT_SQL = "SELECT group_id,data_id,profile,version FROM config WHERE group_id=? AND data_id=? AND profile like ? AND public_status!=" + PublicStatus.DELETE.code();

    private static final String COUNT_CURRENT_SNAPSHOTS_SQL = "SELECT COUNT(*) FROM config WHERE public_status!=?";

    private static final String COUNT_CURRENT_PUBLIC_PROFILE_FILE = "SELECT COUNT(*) FROM config where group_id=? and profile= ? and data_id LIKE ? and public_status != ?";

    private static final String COUNT_CURRENT_UNPUBLIC_PROFILE_FILE = "SELECT COUNT(*) FROM config_candidate where group_id=? and profile= ? and data_id LIKE ? and status not in (?,?)";

    private static final String COUNT_CURRENT_REFERENCE_PROFILE_FILE = "SELECT COUNT(*) FROM config_reference A INNER JOIN config B ON A.group_id=? AND A.profile=? AND A.alias LIKE ? AND A.status!=? " +
            " AND A.ref_group_id = B.group_id AND A.ref_data_id = B.data_id AND A.ref_profile = B.profile WHERE A.type = ?";

    private static final String FIND_CURRENT_SNAPSHOTS_SQL = "SELECT b.group_id AS group_id,a.data_id AS data_id,a.profile AS profile,b.based_version AS based_version,b.version AS edit_version,b.content AS content,a.public_status AS public_status " +
            " FROM config AS a INNER JOIN config_snapshot AS b ON a.group_id=b.group_id AND a.data_id=b.data_id AND a.profile=b.profile AND a.version=b.version AND a.public_status!=? LIMIT ?,?";

    private static final String FIND_CURRENT_SNAPSHOT_SQL = "SELECT a.group_id AS group_id,a.data_id AS data_id,a.profile AS profile,b.based_version AS based_version,b.version AS edit_version,b.content AS content,a.public_status AS public_status " +
            " FROM config AS a INNER JOIN config_snapshot AS b ON a.group_id=? AND a.data_id=? AND a.profile=? AND a.group_id=b.group_id AND a.data_id=b.data_id AND a.profile=b.profile AND a.version=b.version";


    private static final String FIND_CURRENT_SNAPSHOT_IN_GROUP_SQL = "SELECT b.group_id AS group_id," +
            "a.data_id AS data_id,a.profile AS profile,b.based_version AS based_version,b.version AS edit_version," +
            "b.content AS content,a.public_status AS public_status, a.update_time AS update_time " +
            "FROM config AS a INNER JOIN config_snapshot AS b ON a.group_id=? AND a.group_id=b.group_id " +
            "AND a.data_id=b.data_id AND a.profile=b.profile AND a.version=b.version AND a.public_status!=?";

    private static final String SELECT_PUBLISHED_CLAUSE = "SELECT a.group_id group_id, a.profile profile, a.data_id data_id,a.version version, a.public_status public_status, a.update_time update_time, " +
            "b.ref_group_id ref_group_id, b.ref_data_id ref_data_id, b.ref_profile ref_profile, c.type, b.type reftype FROM config a " +
            "left join config_reference b on a.group_id = b.group_id and a.data_id = b.alias and a.profile = b.profile " +
            "left join file_public_status c on a.group_id = c.group_id and a.data_id = c.data_id ";

    private static final String FIND_PUBLISHED_CONFIG = SELECT_PUBLISHED_CLAUSE + " WHERE a.group_id=? AND a.profile=? AND a.public_status!=? ";

    private static final String FIND_PUBLISHED_CONFIGPAGE = SELECT_PUBLISHED_CLAUSE + " WHERE a.group_id=? AND a.profile=? AND a.public_status!=? " + " ORDER BY a.update_time DESC limit ?, ?";

    private static final String FIND_PUBLISHED_CONFIG_IN = SELECT_PUBLISHED_CLAUSE + " WHERE (a.group_id, a.data_id, a.profile) IN %s";

    @Override
    public List<CandidateSnapshot> findCurrentSnapshotsInGroup(String group) {
        return jdbcTemplate.query(FIND_CURRENT_SNAPSHOT_IN_GROUP_SQL,
                SNAPSHOT_MAPPER, group, PublicStatus.DELETE.code());
    }

    @Override
    public int countCurrentSnapshots() {
        return jdbcTemplate.queryForObject(COUNT_CURRENT_SNAPSHOTS_SQL,
                Integer.class,
                PublicStatus.DELETE.code());
    }

    @Override
    public int countPublicFile(String group, String profile, String keyWord) {
        return jdbcTemplate.queryForObject(COUNT_CURRENT_PUBLIC_PROFILE_FILE,
                Integer.class,
                group,profile,"%" + keyWord + "%",PublicStatus.DELETE.code());
    }

    @Override
    public int countUnpublicFile(String group, String profile, String keyWord) {
        return jdbcTemplate.queryForObject(COUNT_CURRENT_UNPUBLIC_PROFILE_FILE,
                Integer.class,
                group, profile, "%" + keyWord + "%" ,StatusType.PUBLISH.code(), StatusType.DELETE.code());
    }

    @Override
    public int countReferenceFile(String group, String profile, String keyWord) {
        return jdbcTemplate.queryForObject(COUNT_CURRENT_REFERENCE_PROFILE_FILE,
                Integer.class,
                group, profile, "%" + keyWord + "%", ReferenceStatus.DELETE.code(),RefType.REFERENCE.value());
    }

    @Override
    public List<CandidateSnapshot> findCurrentSnapshots(final int offset, final int limit) {
        return jdbcTemplate.query(FIND_CURRENT_SNAPSHOTS_SQL,
                SNAPSHOT_MAPPER,
                PublicStatus.DELETE.code(),
                offset,
                limit);
    }

    @Override
    public CandidateSnapshot findCurrentSnapshot(ConfigMeta configMeta) {
        return jdbcTemplate.query(FIND_CURRENT_SNAPSHOT_SQL, PUBLISHED_SNAPSHOT_EXTRACTOR,
                configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile());
    }

    @Override
    public void create(VersionData<ConfigMeta> configId) {
        ConfigMeta configMeta = configId.getData();
        jdbcTemplate.update(INSERT_SQL, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(),
                configId.getVersion(), new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public int batchSave(List<CandidateSnapshot> snapshotList) {
        List<Object[]> params = Lists.newLinkedList();
        for (CandidateSnapshot snapshot : snapshotList) {
            Object[] param = {
                    snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile(),
                    snapshot.getEditVersion(), new Timestamp(System.currentTimeMillis())
            };
            params.add(param);
        }
        int [] results = jdbcTemplate.batchUpdate(INSERT_SQL, params);
        return IntArrayUtil.getSum(results);
    }

    @Override
    public int update(VersionData<ConfigMeta> configId, long oldVersion, PublicStatus publicStatus) {
        ConfigMeta configMeta = configId.getData();
        return jdbcTemplate.update(UPDATE_SQL, configId.getVersion(), publicStatus.code(), configMeta.getGroup(),
                configMeta.getDataId(), configMeta.getProfile(), oldVersion);
    }

    @Override
    public int batchUpdate(List<CandidateSnapshot> candidateSnapshotList) {
        List<Object[]> params = Lists.newLinkedList();
        for (CandidateSnapshot candidateSnapshot : candidateSnapshotList) {
            Object[] param = {
                    candidateSnapshot.getEditVersion(), PublicStatus.INUSE.code(), candidateSnapshot.getGroup(),
                    candidateSnapshot.getDataId(), candidateSnapshot.getProfile()
            };
            params.add(param);
        }
        return IntArrayUtil.getSum(jdbcTemplate.batchUpdate(BATCH_UPDATE_SQL, params));
    }

    @Override
    public ConfigInfoWithoutPublicStatus findCurrentConfigInfo(ConfigMeta configMeta) {
        return jdbcTemplate.query(FIND_CONFIG_SQL, CONFIG_INFO_EXTRACTOR, configMeta.getGroup(),
                configMeta.getDataId(), configMeta.getProfile());
    }

    @Override
    public List<PublishedConfigInfo> findPublished(String group, String profile) {
        return jdbcTemplate.query(FIND_PUBLISHED_CONFIG, new Object[]{group, profile, PublicStatus.DELETE.code()}, PUBLISHED_MAPPER);
    }

    @Override
    public List<PublishedConfigInfo> findPublished(String group, String profile, String dataId) {
        String FIND_PUBLISHED_CONFIG_WITH_DATA_ID = FIND_PUBLISHED_CONFIG + " AND a.data_id = ?";
        return jdbcTemplate.query(FIND_PUBLISHED_CONFIG_WITH_DATA_ID, new Object[]{group, profile, PublicStatus.DELETE.code(), dataId}, PUBLISHED_MAPPER);
    }

    @Override
    public List<PublishedConfigInfo> findPublishedPage(String group, String profile, String dataId, int start, int pageSize) {
        String FIND_PUBLISHED_CONFIG_WITH_DATA_ID = FIND_PUBLISHED_CONFIG + " AND a.data_id = ? " + " ORDER BY a.update_time DESC limit ?, ?";
        return jdbcTemplate.query(FIND_PUBLISHED_CONFIG_WITH_DATA_ID, new Object[]{group, profile, PublicStatus.DELETE.code(), dataId, start, pageSize}, PUBLISHED_MAPPER);
    }

    @Override
    public List<PublishedConfigInfo> findPublishedPage(String group, String profile,int start, int pageSize) {
        return jdbcTemplate.query(FIND_PUBLISHED_CONFIGPAGE, new Object[]{group, profile, PublicStatus.DELETE.code(), start, pageSize}, PUBLISHED_MAPPER);
    }

    @Override
    public List<PublishedConfigInfo> findPublished(List<ConfigMeta> metas) {
        if (CollectionUtils.isEmpty(metas)) {
            return ImmutableList.of();
        }
        String[] params = new String[metas.size() * 3];
        for (int i = 0; i < metas.size(); ++i) {
            ConfigMeta meta = metas.get(i);
            params[3 * i] = meta.getGroup();
            params[3 * i + 1] = meta.getDataId();
            params[3 * i + 2] = meta.getProfile();
        }
        String sql = String.format(FIND_PUBLISHED_CONFIG_IN, SQLUtil.generateGroupStubs(metas.size(), 3));
        return jdbcTemplate.query(sql, params, PUBLISHED_MAPPER);
    }

    @Override
    public List<PublishedConfigInfo> findPublishedWithKeyword(String group, String profile, String keyword) {
        String FIND_PUBLISHED_CONFIG_WITH_KEYWORD = FIND_PUBLISHED_CONFIG + " AND a.data_id LIKE ?";
        Object[] params = {group, profile, PublicStatus.DELETE.code(), "%" + SQLUtil.escapeWildcards(keyword) + "%"};
        return jdbcTemplate.query(FIND_PUBLISHED_CONFIG_WITH_KEYWORD, params, PUBLISHED_MAPPER);
    }

    @Override
    public List<PublishedConfigInfo> findPublishedWithKeywordPage(String group, String profile, String keyword, int start, int pageSize) {
        String FIND_PUBLISHED_CONFIG_WITH_KEYWORD = FIND_PUBLISHED_CONFIG + " AND a.data_id LIKE ?" +" ORDER BY a.update_time DESC limit ?, ?";
        Object[] params = {group, profile, PublicStatus.DELETE.code(), "%" + SQLUtil.escapeWildcards(keyword) + "%", start, pageSize};
        return jdbcTemplate.query(FIND_PUBLISHED_CONFIG_WITH_KEYWORD, params, PUBLISHED_MAPPER);
    }

    @Override
    public List<ConfigInfoWithoutPublicStatus> findPublicedConfigsInProfileAndResources(Environment environment) {
        return jdbcTemplate.query(FIND_PUBLIC_CONFIG_IN_PROFILES_AND_RESOURCES,
                CONFIG_INFO_MAPPER, environment.text() + "%", Environment.RESOURCES.defaultProfile(), PublicStatus.DELETE.code(), PublicType.REFERENCE_MASK, PublicType.REFERENCE_MASK);
    }

    public List<String> findPublicGroupByDataId (String dataId) {
        List<String> result = jdbcTemplate.queryForList("select group_id from file_public_status where data_id = ? ",
                String.class, dataId);
        if(result.isEmpty()){
            return ImmutableList.of();
        }
        return result;
    }

    @Override
    public List<ConfigInfoWithoutPublicStatus> findPublicConfigsInProfileAndResources(Environment environment,
                                                                                      Set<String> groups, String groupLike, String dataIdLike, int page, int pageSize) {
        KeyValuePair<MapSqlParameterSource, String> paramsAndExtraQuery = buildRequestParamsAndExtraQuery(environment, groups, groupLike, dataIdLike, page, pageSize);
        return namedParameterJdbcTemplate.query(String.format(SELECT_PUBLIC_CONFIG_IN_PROFILES_AND_RESOURCE, paramsAndExtraQuery.getValue()),
                paramsAndExtraQuery.getKey(), CONFIG_INFO_MAPPER);
    }

    public int countPublicConfigsInProfileAndResources(Environment environment, Set<String> groups, String groupLike,
                                                       String dataIdLike, int page, int pageSize) {
        KeyValuePair<MapSqlParameterSource, String> paramsAndExtraQuery = buildRequestParamsAndExtraQuery(environment, groups, groupLike, dataIdLike, page, pageSize);
        return namedParameterJdbcTemplate.query(String.format(COUNT_PUBLIC_CONFIG_IN_PROFILES_AND_RESOURCE, paramsAndExtraQuery.getValue()),
                paramsAndExtraQuery.getKey(), SELECT_COUNT_EXTRACTOR);
    }

    private KeyValuePair<MapSqlParameterSource, String> buildRequestParamsAndExtraQuery(Environment environment, Set<String> groups, String groupLike,
                                                                                        String dataIdLike, int page, int pageSize) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        long offset = PaginationUtil.start(page, pageSize);
        params.addValue("offset", offset);
        params.addValue("limit", pageSize);
        params.addValue("groups", groups);
        params.addValue("profile_like", environment.text() + "%");
        params.addValue("profile_res", Environment.RESOURCES.defaultProfile());
        params.addValue("status_del", PublicStatus.DELETE.code());
        params.addValue("mask_ref", PublicType.REFERENCE_MASK);
        StringBuilder extraQuery = new StringBuilder();
        if (!Strings.isNullOrEmpty(groupLike)) {
            extraQuery.append(" AND b.group_id like :group_like ");
            params.addValue("group_like", "%" + SQLUtil.escapeWildcards(groupLike) + "%");
        }
        if (!Strings.isNullOrEmpty(dataIdLike)) {
            extraQuery.append(" AND b.data_id like :data_id_like ");
            params.addValue("data_id_like", "%" + SQLUtil.escapeWildcards(dataIdLike) + "%");
        }
        return new KeyValuePair<>(params, extraQuery.toString());
    }

    @Override
    public List<VersionData<ConfigMeta>> findInEnvironment(String group, String dataId, Environment environment) {
        return jdbcTemplate.query(FIND_IN_ENVIRONMENT_SQL,
                CONFIG_ID_MAPPER, group, dataId, environment.defaultProfile() + "%");
    }

    @Override
    public int delete(VersionData<ConfigMeta> configId, long oldVersion) {
        ConfigMeta configMeta = configId.getData();
        return jdbcTemplate.update(DELETE_SQL, PublicStatus.DELETE.code(), configMeta.getGroup(), configMeta.getDataId(),
                configMeta.getProfile(), oldVersion);
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update("DELETE FROM config WHERE group_id=? AND data_id=? AND profile=?", meta.getGroup(), meta.getDataId(), meta.getProfile());
    }


    private static final RowMapper<VersionData<ConfigMeta>> CONFIG_ID_MAPPER = new RowMapper<VersionData<ConfigMeta>>() {
        @Override
        public VersionData<ConfigMeta> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return VersionData.of(rs.getLong("version"), new ConfigMeta(rs.getString("group_id"),
                    rs.getString("data_id"), rs.getString("profile")));
        }
    };

    private static final ResultSetExtractor<ConfigInfoWithoutPublicStatus> CONFIG_INFO_EXTRACTOR = new ResultSetExtractor<ConfigInfoWithoutPublicStatus>() {
        @Override
        public ConfigInfoWithoutPublicStatus extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new ConfigInfoWithoutPublicStatus(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"),
                        rs.getLong("version"), PublicStatus.isInUse(rs.getInt("public_status")),
                        rs.getTimestamp("update_time"));
            } else {
                return null;
            }
        }
    };

    private static final ResultSetExtractor<CandidateSnapshot> PUBLISHED_SNAPSHOT_EXTRACTOR = new ResultSetExtractor<CandidateSnapshot>() {
        @Override
        public CandidateSnapshot extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                final int publicStatus = rs.getInt("public_status");
                final StatusType type = publicStatus == PublicStatus.DELETE.code() ? StatusType.DELETE : StatusType.PUBLISH;
                return new CandidateSnapshot(new Candidate(rs.getString("group_id"),
                        rs.getString("data_id"),
                        rs.getString("profile"),
                        rs.getLong("based_version"),
                        rs.getLong("edit_version"),
                        type),
                        rs.getString("content"), "");
            }
            return null;
        }
    };

    private static final RowMapper<PublishedConfigInfo> PUBLISHED_MAPPER = new RowMapper<PublishedConfigInfo>() {
        @Override
        public PublishedConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PublishedConfigInfo result = new PublishedConfigInfo();
            String groupId = rs.getString("group_id");
            String dataId = rs.getString("data_id");
            String profile = rs.getString("profile");

            result.setConfigMeta(new ConfigMeta(groupId, dataId, profile));
            result.setVersion(rs.getLong("version"));
            result.setUpdateTime(rs.getTimestamp("update_time"));
            result.setPublicStatus(PublicStatus.codeOf(rs.getInt("public_status")));
            result.setStatusType(StatusType.PUBLISH);
            String refGroupId = rs.getString("ref_group_id");
            String refDataId = rs.getString("ref_data_id");
            String refProfile = rs.getString("ref_profile");
            if (!Strings.isNullOrEmpty(refGroupId)
                    && !Strings.isNullOrEmpty(refDataId)
                    && !Strings.isNullOrEmpty(refProfile)) {
                result.setRefConfigMeta(new ConfigMeta(refGroupId, refDataId, refProfile));
                int reftype = rs.getInt("reftype");
                result.setRefType(RefType.codeOf(reftype));
            } else {//只有第一层级的才会有publicType
                int type = rs.getInt("type");
                if (type != 0) {
                    result.setPublicType(new PublicType(type));
                }
            }
            return result;
        }
    };

    private static final RowMapper<ConfigInfoWithoutPublicStatus> CONFIG_INFO_MAPPER = new RowMapper<ConfigInfoWithoutPublicStatus>() {
        @Override
        public ConfigInfoWithoutPublicStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            String groupId = rs.getString("group_id");
            String dataId = rs.getString("data_id");
            String profile = rs.getString("profile");
            ConfigInfoWithoutPublicStatus configStatus = new ConfigInfoWithoutPublicStatus(groupId, dataId, profile,
                    rs.getLong("version"), PublicStatus.isInUse(rs.getInt("public_status")),
                    rs.getTimestamp("update_time"));
            return configStatus;
        }
    };

    private static final ResultSetExtractor<Integer> SELECT_COUNT_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("total_count");
            }
            return null;
        }
    };

    private static final RowMapper<CandidateSnapshot> SNAPSHOT_MAPPER = new RowMapper<CandidateSnapshot>() {
        @Override
        public CandidateSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            final int publicStatus = rs.getInt("public_status");
            final StatusType type = publicStatus == PublicStatus.DELETE.code() ? StatusType.DELETE : StatusType.PUBLISH;
            return new CandidateSnapshot(new Candidate(rs.getString("group_id"),
                    rs.getString("data_id"),
                    rs.getString("profile"),
                    rs.getLong("based_version"),
                    rs.getLong("edit_version"),
                    type,
                    rs.getTimestamp("update_time")),
                    rs.getString("content"), "");
        }
    };

}
