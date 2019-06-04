package qunar.tc.qconfig.admin.dao.impl;

import com.google.gson.Gson;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.ApplicationDao;
import qunar.tc.qconfig.common.support.Application;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pingyang.yang on 2018/11/26
 */
@Repository
public class ApplicationDaoImpl implements ApplicationDao {

    private static final String SELECT_BY_APP_CODE = "SELECT code, name, group_code, mail_group, status, creator FROM pb_app WHERE code = ?";

    private static final String SELECT_BY_APP_CODES = "SELECT code, name, group_code, mail_group, status, creator FROM pb_app WHERE code in (:id)";

    private static final String INSERT_APP = "INSERT INTO pb_app (code, name, group_code, mail_group, status, creator) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String CHECK_EXIST_SQL = "SELECT code FROM pb_app WHERE code = ?";

    private static final String UPDATE_APP_MAIL = "UPDATE pb_app SET mail_group = ? WHERE code = ? ";

    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private Gson gson = new Gson();

    @Override
    public List<Application> getApplicationsByAppCode(List<String> appCode) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", appCode);
        return namedParameterJdbcTemplate.query(SELECT_BY_APP_CODES, parameters, APPLICATIONS_MAPPER);
    }

    @Override
    public boolean checkExist(String appCode) {
        String result = jdbcTemplate.query(CHECK_EXIST_SQL, CODE_MAPPER, appCode);
        return result != null;
    }

    @Override
    public int updateApplicationMail(Application application) {
        return jdbcTemplate.update(UPDATE_APP_MAIL, gson.toJson(application.getMailGroup()), application.getCode());
    }

    @Override
    public Application getApplicationByAppCode(String appCode) {
        return jdbcTemplate.query(SELECT_BY_APP_CODE, APPLICATION_MAPPER, appCode);
    }

    @Override
    public int createApplication(Application application) {
        return jdbcTemplate.update(INSERT_APP, application.getCode(), application.getName(), application.getGroupCode(), gson.toJson(application.getMailGroup()), application.getStatus().code(), application.getCreator());
    }

    private static ResultSetExtractor<Application> APPLICATION_MAPPER = new ResultSetExtractor<Application>() {
        @Override
        public Application extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return getApplicationFromRs(resultSet);
            }
            return null;
        }
    };

    private static RowMapper<Application> APPLICATIONS_MAPPER = new RowMapper<Application>() {
        @Override
        public Application mapRow(ResultSet resultSet, int i) throws SQLException {
            return getApplicationFromRs(resultSet);
        }
    };

    private static Application getApplicationFromRs(ResultSet resultSet) throws SQLException {
        String code = resultSet.getString("code");
        String name = resultSet.getString("name");
        String groupCode = resultSet.getString("group_code");
        Application.Status status = Application.Status.codeOf(resultSet.getInt("status"));
        String creator = resultSet.getString("creator");
        return new Application(code, name, groupCode, status, creator);
    }

    private static ResultSetExtractor<String> CODE_MAPPER = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getString("code");
            }
            return null;
        }
    };
}
