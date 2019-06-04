package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.model.InheritConfigMeta;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PublicStatus;
import qunar.tc.qconfig.servercommon.bean.ReferenceStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 配置文件继承相关dao
 * Created by chenjk on 2017/4/1.
 */
@Repository("inheritConfigDao")
public class InheritConfigDaoImpl extends ReferenceDaoImpl {

    public boolean childFileExists(ConfigMeta childConfigMeta) {
        String sql = "select count(1) as count from config_reference a" +
                " inner join config b" +
                " on a.group_id = b.group_id and a.alias = b.data_id and a.profile = b.profile" +
                " and a.group_id = ? and a.alias = ? and a.profile = ?" +
                " and a.type = ? and a.status != ? and b.public_status != ?";
        return jdbcTemplate.query(sql, new Object[]{childConfigMeta.getGroup(),
                childConfigMeta.getDataId(), childConfigMeta.getProfile(),
                RefType.INHERIT.value(), ReferenceStatus.DELETE.code(),
                PublicStatus.DELETE.code()}, EXISTS_MAPPER);
    }

    public boolean parentFileExists(String groupId, String dataId, String profile) {
        String sql = "SELECT count(1) as count from file_public_status a" +
                " inner join config b" +
                " on a.group_id = b.group_id and a.data_id = b.data_id" +
                " and a.group_id = ? and a.data_id = ? and (a.type & ?) = ? and b.public_status != ?" +
                " and b.profile = ?";
        return (jdbcTemplate.query(sql, new Object[]{
                groupId, dataId, PublicType.INHERIT_MASK, PublicType.INHERIT_MASK,
                PublicStatus.DELETE.code(), profile}, EXISTS_MAPPER));
    }

    public boolean parentFileExistsInOtherGroup(String group, String dataId) {
        String sql = "SELECT count(1) as count from file_public_status a" +
                " inner join config b" +
                " on a.group_id = b.group_id and a.data_id = b.data_id" +
                " and a.group_id != ? and a.data_id = ? and (a.type & ?)= ? and b.public_status != ?";
        return jdbcTemplate.query(sql, new Object[]{
                group, dataId, PublicType.INHERIT_MASK, PublicType.INHERIT_MASK,
                PublicStatus.DELETE.code()}, EXISTS_MAPPER);
    }

    /**
     *
     * 分页查询可继承文件，满足如下条件为可被当前group继承的文件
     * <ol>
     *  <li>非本group下的文件；</li>
     *  <li>本profile或者resources下文件；</li>
     *  <li>没有被本group继承过的文件；</li>
     *  <li>被标记为可继承文件；</li>
     *  <li>没有被删除的文件；</li>
     *  <li>子环境不能继承其他子环境的配置，可以继承父环境和resources下配置</li>
     * </ol>
     */
    public List<ConfigMeta> inheritableFile(String groupId, String profile, String term, long start, long offset) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("groupId", groupId);
        parameters.addValue("profiledefault", Environment.fromProfile(profile).defaultProfile());
        parameters.addValue("profile", profile);
        parameters.addValue("resources", Environment.RESOURCES.defaultProfile());
        parameters.addValue("term", "%" + term + '%');
        parameters.addValue("inheritmask", PublicType.INHERIT_MASK);
        parameters.addValue("start", start);
        parameters.addValue("offset", offset);
        parameters.addValue("delete_num", "2");
        parameters.addValue("public_status", PublicStatus.DELETE.code());

        String sql = "select a.group_id, a.data_id, b.profile from file_public_status a" +
                " inner join config b on a.group_id = b.group_id and a.data_id = b.data_id" +
                " and (b.profile = :profile or b.profile = :resources or b.profile = :profiledefault)" +
                " and (a.group_id != :groupId) and (a.type & :inheritmask)= :inheritmask" +
                " and (a.group_id like :term or a.data_id like :term)" +
                " and b.public_status != :public_status";

        if (Strings.isNullOrEmpty(term)) {//term 为空的情况
            sql = "select a.group_id, a.data_id, b.profile from file_public_status a" +
                    " inner join config b on a.group_id = b.group_id and a.data_id = b.data_id" +
                    " and (b.profile = :profile or b.profile = :resources or b.profile = :profiledefault)" +
                    " and (a.group_id != :groupId) and (a.type & :inheritmask)= :inheritmask" +
                    " and b.public_status != :public_status";

        }
        return namedParameterJdbcTemplate.query(sql, parameters, INHERIT_CONFIG_META_MAPPER);
    }

    public long countInheritableFiles(String groupId, String profile, String term) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("groupId", groupId);
        parameters.addValue("profile", profile);
        parameters.addValue("profiledefault", Environment.fromProfile(profile).defaultProfile());
        parameters.addValue("resources", Environment.RESOURCES.defaultProfile());
        parameters.addValue("term", "%" + term + '%');
        parameters.addValue("inheritmask", PublicType.INHERIT_MASK);
        parameters.addValue("delete_num", "2");
        parameters.addValue("public_status", PublicStatus.DELETE.code());

        String sql = "select count(1) as count from file_public_status a" +
                " inner join config b on a.group_id = b.group_id and a.data_id = b.data_id" +
                " and (b.profile = :profile or b.profile = :resources or b.profile = :profiledefault)" +
                " and (a.group_id != :groupId) and (a.type & :inheritmask)= :inheritmask" +
                " and (a.group_id like :term or a.data_id like :term) " +
                " and b.public_status != :public_status";
        if (Strings.isNullOrEmpty(term)) {
            sql = "select count(1) as count from file_public_status a" +
                    " inner join config b on a.group_id = b.group_id and a.data_id = b.data_id" +
                    " and (b.profile = :profile or b.profile = :resources or b.profile = :profiledefault)" +
                    " and (a.group_id != :groupId) and (a.type & :inheritmask)= :inheritmask" +
                    " and b.public_status != :public_status";
        }
        return namedParameterJdbcTemplate.query(sql, parameters, COUNT_MAPPER);
    }

    public int save(CandidateDTO dto, String operator) {
        Reference reference = new Reference();
        reference.setType(RefType.INHERIT.value());
        reference.setProfile(dto.getProfile());
        reference.setAlias(dto.getDataId());
        reference.setGroup(dto.getGroup());
        reference.setOperator(operator);
        reference.setRefDataId(dto.getInheritDataId());
        reference.setRefGroup(dto.getInheritGroupId());
        reference.setRefProfile(dto.getInheritProfile());
        return create(reference);
    }

    public InheritConfigMeta queryDetail(InheritConfigMeta meta) {
        ConfigMeta configMeta = new ConfigMeta(meta.getGroupId(), meta.getDataId(), meta.getProfile());
        ConfigMeta refConfigMeta = findReference(configMeta, RefType.INHERIT.value());
        if (refConfigMeta == null) {
            return null;
        }
        InheritConfigMeta inheritConfigMeta = new InheritConfigMeta();
        inheritConfigMeta.setGroupId(configMeta.getGroup());
        inheritConfigMeta.setDataId(configMeta.getDataId());
        inheritConfigMeta.setProfile(configMeta.getProfile());
        inheritConfigMeta.setInheritGroupId(refConfigMeta.getGroup());
        inheritConfigMeta.setInheritDataId(refConfigMeta.getDataId());
        inheritConfigMeta.setInheritProfile(refConfigMeta.getProfile());
        return inheritConfigMeta;
    }

    public ConfigMeta findChildMetaInUsed(ConfigMeta parentConfigMeta, String ip) {
        String sql = "select a.group_id, a.alias data_id, a.profile from config_reference a" +
                " inner join config_used_log b" +
                " on a.group_id = b.group_id and a.alias = b.data_id and a.profile = b.profile" +
                " and a.ref_group_id = ? and a.ref_data_id = ? and a.ref_profile = ?" +
                " and b.ip = inet_aton(?) and a.type = ?";
        return jdbcTemplate.query(sql, new Object[]{parentConfigMeta.getGroup(), parentConfigMeta.getDataId(),
                parentConfigMeta.getProfile(), ip, RefType.INHERIT.value()}, CONFIMETA_MAPPER);
    }

    private static final ResultSetExtractor<Boolean> EXISTS_MAPPER = new ResultSetExtractor<Boolean>() {
        @Override
        public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("count") != 0;
            } else {
                return false;
            }
        }
    };

    private static final ResultSetExtractor<Long> COUNT_MAPPER = new ResultSetExtractor<Long>() {
        @Override
        public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getLong("count");
            } else {
                return 0l;
            }
        }
    };

    private static final RowMapper<ConfigMeta> INHERIT_CONFIG_META_MAPPER = new RowMapper<ConfigMeta>() {
        @Override
        public ConfigMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
        }
    };

    private static final ResultSetExtractor<ConfigMeta> CONFIMETA_MAPPER = new ResultSetExtractor<ConfigMeta>() {
        @Override
        public ConfigMeta extractData(ResultSet rs) throws SQLException, DataAccessException {
            if(rs.next()) {
                return new ConfigMeta(rs.getString("group_id"), rs.getString("data_id"), rs.getString("profile"));
            } else {
                return null;
            }
        }
    };

}
