package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.EncryptKeyDao;
import qunar.tc.qconfig.admin.model.EncryptKey;
import qunar.tc.qconfig.admin.model.EncryptKeyStatus;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 11:19
 */
@Repository
public class EncryptKeyDaoImpl implements EncryptKeyDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<EncryptKey> ENCRYPT_KEY_ROW_MAPPER = new RowMapper<EncryptKey>() {
        @Override
        public EncryptKey mapRow(final ResultSet resultSet, final int i)
                throws SQLException {
            return new EncryptKey(resultSet.getString("encrypt_key"),
                    EncryptKeyStatus.codeOf(resultSet.getInt("status")));
        }
    };

    @Override
    public List<String> selectEncryptedKeys(String group, String dataId) {
        List<String> result = jdbcTemplate.queryForList("SELECT encrypt_key FROM encrypt_key WHERE group_id=? and data_id=? and status=?",
                String.class, group, dataId, EncryptKeyStatus.ENCRYPTED.code());
        if (result == null) {
            return ImmutableList.of();
        }
        return result;
    }

    @Override
    public List<EncryptKey> select(final String group, final String dataId) {
        List<EncryptKey> result = jdbcTemplate.query(
                "SELECT encrypt_key,status FROM encrypt_key WHERE group_id=? AND data_id=?",
                ENCRYPT_KEY_ROW_MAPPER,
                group,
                dataId);
        if (result == null) {
            return ImmutableList.of();
        }

        return result;
    }

    @Override
    public void insertOrUpdate(final String group, final String dataId, final String operator, final List<EncryptKey> keys) {
        String sql = "INSERT INTO encrypt_key(group_id,data_id,encrypt_key,operator,status,create_time) VALUES(?,?,?,?,?,now()) on duplicate key update operator=?,status=?";
        List<Object[]> params = Lists.newLinkedList();
        for (EncryptKey key : keys) {
            Object[] param = {group, dataId, key.getKey(), operator, key.getStatus().code(), operator, key.getStatus().code()};
            params.add(param);
        }
        jdbcTemplate.batchUpdate(sql, params);
    }
}
