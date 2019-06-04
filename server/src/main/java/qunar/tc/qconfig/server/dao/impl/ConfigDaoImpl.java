package qunar.tc.qconfig.server.dao.impl;

import com.google.common.base.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.server.domain.RelationMeta;
import qunar.tc.qconfig.servercommon.bean.*;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * User: zhaohuiyu Date: 5/14/14 Time: 2:58 PM
 */
@Repository
public class ConfigDaoImpl implements ConfigDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<VersionData<ConfigMeta>> loadAll() {
        return jdbcTemplate.query("SELECT group_id,data_id,profile,version FROM config WHERE public_status!=?", CONFIGID_MAPPER, PublicStatus.DELETE.code());
    }

    @Override
    public List<VersionData<ConfigMeta>> loadByGroupAndProfile(String group, String profile) {
        return jdbcTemplate.query("SELECT group_id,data_id,profile,version FROM config WHERE public_status!=? AND group_id=? AND profile=?", CONFIGID_MAPPER, PublicStatus.DELETE.code(), group, profile);
    }

    @Override
    public ChecksumData<String> loadFromCandidateSnapshot(VersionData<ConfigMeta> configId) {
        ConfigMeta configMeta = configId.getData();
        long version = configId.getVersion();
        String content = jdbcTemplate
                .query("SELECT content FROM config_candidate_snapshot WHERE group_id=? AND data_id=? AND profile=? AND edit_version=?",
                        CONTENT_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), version);
        return content == null ? null : ChecksumData.of(ChecksumAlgorithm.getChecksum(content), content);
    }

    @Override
    public ChecksumData<String> load(VersionData<ConfigMeta> configId) {
        ConfigMeta configMeta = configId.getData();
        return jdbcTemplate
                .query("SELECT checksum,content FROM config_snapshot WHERE group_id=? AND data_id=? AND profile=? AND version=?",
                        CONFIG_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(),
                        configId.getVersion());
    }

    @Override
    public VersionData<ConfigMeta> load(ConfigMeta configMeta) {
        return jdbcTemplate.query(
                "SELECT group_id,data_id,profile,version FROM config WHERE group_id=? AND data_id=? AND profile=? AND public_status!=?",
                CONFIGID_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), PublicStatus.DELETE.code());
    }

    @Override
    public Long selectBasedVersion(VersionData<ConfigMeta> configId) {
        ConfigMeta configMeta = configId.getData();
        return jdbcTemplate
                .query("SELECT based_version FROM config_candidate_snapshot WHERE group_id=? AND data_id=? AND profile=? AND edit_version=?",
                        BASED_VERSION_EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(),
                        configMeta.getProfile(), configId.getVersion());
    }

    /**
     * 按照refer类型查数据
     * @param source
     * @param refType
     * @return
     */
    @Override
    public Optional<ConfigMeta> loadReference(ConfigMeta source, RefType refType) {
        return jdbcTemplate
                .query("SELECT ref_group_id,ref_data_id,ref_profile FROM config_reference WHERE group_id=? and profile=? and alias=? and status!=? and type = ?",
                        META_EXTRACTOR, source.getGroup(), source.getProfile(), source.getDataId(),
                        ReferenceStatus.DELETE.code(), refType.value());
    }

    /**
     * 返回所有的引用和继承关系，在cacheservice再区分引用和继承
     * @return
     */
    @Override
    public List<ReferenceInfo> loadAllReferenceInfo() {
        return jdbcTemplate
                .query("SELECT ref.group_id, ref.profile, ref.alias, ref.ref_group_id, ref.ref_data_id, ref.ref_profile, ref.type FROM config_reference ref WHERE ref.type = ? or (ref.type = ? and " +
                                "exists (select conf.group_id, conf.data_id, conf.profile from config conf where conf.group_id = ref.group_id and conf.data_id = ref.alias and conf.profile = ref.profile and conf.public_status != ?) " +
                                "and exists (select conf.group_id, conf.data_id, conf.profile from config conf where conf.group_id = ref.ref_group_id and conf.data_id = ref.ref_data_id and conf.profile = ref.ref_profile and conf.public_status != ?))" +
                                "and ref.status!=?",
                        REFERENCE_INFO_MAPPER, RefType.REFERENCE.value(), RefType.INHERIT.value(), PublicStatus.DELETE.code(), PublicStatus.DELETE.code(), ReferenceStatus.DELETE.code());
    }

    @Override
    public Optional<ReferenceInfo> loadReferenceInfo(RelationMeta relationMeta) {
        ConfigMeta source = relationMeta.getSource();
        ConfigMeta target = relationMeta.getTarget();
        return jdbcTemplate.query("SELECT ref_group_id, ref_data_id, ref_profile, group_id, alias, profile, type " +
                        "FROM config_reference WHERE group_id=? and profile=? and alias=? " +
                        "and ref_group_id=? and ref_profile=? and ref_data_id=? and status!=?",
                        REFINFO_EXTRACTOR, source.getGroup(), source.getProfile(), source.getDataId(),
                        target.getGroup(), target.getProfile(), target.getDataId(),
                        ReferenceStatus.DELETE.code());
    }

    @Override
    public Optional<ReferenceInfo> loadReferenceInfo(String groupId, String dataId, String profile, String refGroupId,
                                                     String refDataId, String refProfile, RefType refType) {
        return jdbcTemplate
                .query("SELECT ref_group_id,ref_data_id,ref_profile, group_id, alias, profile, type FROM config_reference WHERE group_id=? and profile=? and alias=? and ref_group_id = ? and ref_data_id = ? and ref_profile = ? and status!=? and type = ?",
                        REFINFO_EXTRACTOR, groupId,  profile, dataId, refGroupId, refDataId, refProfile,
                        ReferenceStatus.DELETE.code(), refType.value());
    }

    private static final RowMapper<ReferenceInfo> REFERENCE_INFO_MAPPER = new RowMapper<ReferenceInfo>() {
        @Override
        public ReferenceInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReferenceInfo(new ConfigMeta(rs.getString("group_id"), rs.getString("alias"),
                    rs.getString("profile")), new ConfigMeta(rs.getString("ref_group_id"), rs.getString("ref_data_id"),
                    rs.getString("ref_profile")), RefType.codeOf(rs.getInt("type")));
        }
    };

    private static final ResultSetExtractor<Optional<ConfigMeta>> META_EXTRACTOR = new ResultSetExtractor<Optional<ConfigMeta>>() {
        @Override
        public Optional<ConfigMeta> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return Optional.of(new ConfigMeta(rs.getString("ref_group_id"), rs.getString("ref_data_id"), rs.getString("ref_profile")));
            } else {
                return Optional.absent();
            }
        }
    };

    private static final ResultSetExtractor<Optional<ReferenceInfo>> REFINFO_EXTRACTOR = new ResultSetExtractor<Optional<ReferenceInfo>>() {
        @Override
        public Optional<ReferenceInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                String groupId = rs.getString("group_id");
                String dataId = rs.getString("alias");
                String profile = rs.getString("profile");
                String refGroupId = rs.getString("ref_group_id");
                String refDataId = rs.getString("ref_data_id");
                String refProfile = rs.getString("ref_profile");
                RefType refType = RefType.codeOf(rs.getInt("type"));
                return Optional.of(new ReferenceInfo(new ConfigMeta(groupId, dataId, profile) , new ConfigMeta(refGroupId, refDataId, refProfile), refType));
            }
            return Optional.absent();
        }
    };

    private static final RowMapper<VersionData<ConfigMeta>> CONFIGID_MAPPER = new RowMapper<VersionData<ConfigMeta>>() {
        @Override
        public VersionData<ConfigMeta> mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigMeta configMeta = new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"),
                    rs.getString("profile"));
            return VersionData.of(rs.getLong("version"), configMeta);
        }
    };

    private static final ResultSetExtractor<VersionData<ConfigMeta>> CONFIGID_EXTRACTOR = new ResultSetExtractor<VersionData<ConfigMeta>>() {
        @Override
        public VersionData<ConfigMeta> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                ConfigMeta configMeta = new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"),
                        rs.getString("profile"));
                return VersionData.of(rs.getLong("version"), configMeta);
            }
            return null;
        }
    };

    private static final ResultSetExtractor<ChecksumData<String>> CONFIG_EXTRACTOR = new ResultSetExtractor<ChecksumData<String>>() {
        @Override
        public ChecksumData<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return ChecksumData.of(rs.getString("checksum"), rs.getString("content"));
            }
            return null;
        }
    };

    private static final ResultSetExtractor<Long> BASED_VERSION_EXTRACTOR = new ResultSetExtractor<Long>() {
        @Override
        public Long extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getLong("based_version");
            }
            return null;
        }
    };

    private static final ResultSetExtractor<String> CONTENT_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("content");
            }
            return null;
        }
    };
}
