package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Strings;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ApplicationUserDao;
import qunar.tc.qconfig.admin.model.AccessRoleType;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pingyang.yang on 2018/11/26
 */
@Repository
public class ApplicationUserDaoImpl implements ApplicationUserDao {

    @Resource
    JdbcTemplate jdbcTemplate;

    private static final String GET_APP_BY_RTX_AND_ROLE = "SELECT app_code FROM pb_user_app WHERE login_id = ? AND role_code = ?";

    private static final String GET_APP_BY_RTX = "SELECT app_code FROM pb_user_app WHERE login_id = ? ";

    private static final String GET_ROLE_BY_RTX_APPCODE = "SELECT role_code FROM pb_user_app WHERE login_id = ? AND app_code = ?";

    private static final String ADD_USER_FOR_APP = "INSERT IGNORE pb_user_app (app_code, role_code, login_id) VALUES (?, ?, ?)";

    private static final String REMOVE_USER_FROM_APP = "DELETE FROM pb_user_app WHERE login_id = ? AND app_code = ?";

    private static final String GET_USER_BY_APP = "SELECT login_id FROM pb_user_app WHERE app_code = ? AND role_code = ?";

    @Override
    public List<String> getAppCodeByRTX(String rtxId, AccessRoleType type) {
        return jdbcTemplate.query(GET_APP_BY_RTX_AND_ROLE, LIST_APPCODE, rtxId, type.code());
    }

    @Override
    public List<String> getAppCodeByRTX(String rtxId) {
        return jdbcTemplate.query(GET_APP_BY_RTX, LIST_APPCODE, rtxId);
    }

    @Override
    public AccessRoleType getRoleByIDAndAppCode(String rtxId, String appCode) {
        return jdbcTemplate.query(GET_ROLE_BY_RTX_APPCODE, SINGLE_USER_TYPE, rtxId, appCode);
    }

    @Override
    public List<String> getUserByAppCodeAndRole(String appCode, AccessRoleType type) {
        return jdbcTemplate.query(GET_USER_BY_APP, LIST_USER, appCode, type.code());
    }

    @Override
    public int addAccess(String rtxId, String appCode, AccessRoleType type) {
        return jdbcTemplate.update(ADD_USER_FOR_APP, appCode, type.code(), rtxId);
    }

    @Override
    public void batchAdd(final List<String> rtxId, final String addCode, final AccessRoleType type) {
        jdbcTemplate.batchUpdate(ADD_USER_FOR_APP, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setString(1, addCode);
                preparedStatement.setString(3, rtxId.get(i));
                preparedStatement.setInt(2, type.code());
            }

            @Override
            public int getBatchSize() {
                return rtxId.size();
            }
        });
    }

    @Override
    public int removeAccess(String rtxId, String appCode) {
        return jdbcTemplate.update(REMOVE_USER_FROM_APP, rtxId, appCode);
    }

    private static final RowMapper<String> LIST_APPCODE = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("app_code");
        }
    };

    private static final RowMapper<String> LIST_USER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("login_id");
        }
    };

    private static final ResultSetExtractor<String> SINGLE_APPCODE = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getString("app_code");
            }
            return Strings.nullToEmpty(null);
        }
    };

    private static final ResultSetExtractor<AccessRoleType> SINGLE_USER_TYPE = new ResultSetExtractor<AccessRoleType>() {
        @Override
        public AccessRoleType extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                int type = resultSet.getInt("role_code");
                return AccessRoleType.codeOf(type);
            }
            return null;
        }
    };
}
