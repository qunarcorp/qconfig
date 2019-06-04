package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.model.ApiPermission;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by chenjk on 2018/1/12.
 */
@Repository
public class ApiGroupIdPermissionRelDaoImpl {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<ApiPermission> queryByGroupIdAndTargetGroupId(String groupId, String targetGroupId) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("groupId", groupId);
        parameters.addValue("targetGroupId", targetGroupId);
        String sql = "select c.* from api_groupid_rel a " +
                "left join api_groupid_permission_rel b on a.id = b.groupid_rel_id " +
                "LEFT JOIN api_permission c on b.permission_id = c.id " +
                "where a.groupid=:groupId and a.target_groupid=:targetGroupId";

        return namedParameterJdbcTemplate.query(sql,
                parameters,
                ApiPermissionDaoImpl.API_PERMISSION_MAPPER);
    }

    public void save(Long groupidRelId, List<Long> permissionIds) {
        String sql = "insert into api_groupid_permission_rel (groupid_rel_id, permission_id) values (?, ?)";
        jdbcTemplate.batchUpdate(sql, genParams(groupidRelId, permissionIds));
    }

    public List<Object[]> genParams(Long groupidRelId, List<Long> permissionIds) {
        List<Object[]> params = Lists.newArrayList();
        for (Long permissionId : permissionIds) {
            Object[] param = new Object[]{groupidRelId, permissionId};
            params.add(param);
        }
        return params;
    }

    public void delete(Long groupidRelId, List<Long> permissionIds) {
        String sql = "delete from api_groupid_permission_rel where groupid_rel_id = ? and permission_id = ?";
        jdbcTemplate.batchUpdate(sql, genParams(groupidRelId, permissionIds));
    }

    public void deleteAllGroupidRefPermissions(Long groupidRelId) {
        String sql = "delete from api_groupid_permission_rel where groupid_rel_id = ?";
        jdbcTemplate.update(sql, groupidRelId);
    }

    public void deleteByPermissionId(Long permissionId) {
        String sql = "delete from api_groupid_permission_rel where permission_id = ?";
        jdbcTemplate.update(sql, permissionId);
    }
}
