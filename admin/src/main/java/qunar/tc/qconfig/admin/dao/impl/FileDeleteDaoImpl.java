package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileDeleteDao;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.DbOpType;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/5/15 18:21
 */
@Repository
public class FileDeleteDaoImpl implements FileDeleteDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insert(final ConfigMeta meta, final List<String> envIps) {
        insert(jdbcTemplate, meta, envIps);
    }

    @Override
    public boolean exist(ConfigMeta meta) {
        String sql = "SELECT count(*) FROM file_delete WHERE group_id=? AND data_id=? AND profile= ? ";
        return (jdbcTemplate.queryForObject(sql, Integer.class, meta.getGroup(), meta.getDataId(), meta.getProfile()) > 0);
    }

    private void insert(JdbcTemplate jdbcTemplate, ConfigMeta meta, List<String> ips) {
        String sql = "INSERT IGNORE INTO file_delete(group_id, data_id, profile, ip) VALUES(?, ?, ?, INET_ATON(?))";
        List<Object[]> params = Lists.newLinkedList();
        for (String ip : ips) {
            Object[] param = {
                    meta.getGroup(),
                    meta.getDataId(),
                    meta.getProfile(),
                    ip
            };
            params.add(param);
        }
        jdbcTemplate.batchUpdate(sql, params);
    }
}
