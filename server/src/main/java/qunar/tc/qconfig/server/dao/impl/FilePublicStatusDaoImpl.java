package qunar.tc.qconfig.server.dao.impl;

import com.google.common.base.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.server.dao.FilePublicStatusDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:02
 */
@Repository
public class FilePublicStatusDaoImpl implements FilePublicStatusDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<PublicConfigInfo> loadAll() {
        return jdbcTemplate.query("SELECT group_id, data_id, type FROM file_public_status", META_MAPPER);
    }

    public Optional<PublicConfigInfo> loadPublicInfo(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        return jdbcTemplate.query("SELECT group_id, data_id, type FROM file_public_status where group_id = ? AND data_id= ? ", META_EXTRACTOR, configMetaWithoutProfile.getGroup(), configMetaWithoutProfile.getDataId());
    }

    @Override
    public boolean exist(PublicConfigInfo file) {
        return jdbcTemplate.query("SELECT id FROM file_public_status WHERE group_id=? AND data_id=?", ID_EXTRACTOR, file.getConfigMetaWithoutProfile().getGroup(), file.getConfigMetaWithoutProfile().getDataId()) != null;
    }

    private static final RowMapper<PublicConfigInfo> META_MAPPER = new RowMapper<PublicConfigInfo>() {
        @Override
        public PublicConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PublicConfigInfo(new ConfigMetaWithoutProfile(rs.getString("group_id"), rs.getString("data_id")), new PublicType(rs.getInt("type")));
        }
    };

    private static final ResultSetExtractor<Integer> ID_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("id");
            }
            return null;
        }
    };

    private static final ResultSetExtractor<Optional<PublicConfigInfo>> META_EXTRACTOR = new ResultSetExtractor<Optional<PublicConfigInfo>>() {
        @Override
        public Optional<PublicConfigInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return Optional.of(new PublicConfigInfo(new ConfigMetaWithoutProfile(rs.getString("group_id"), rs.getString("data_id")), new PublicType(rs.getInt("type"))));
            }
            return Optional.absent();
        }
    };
}
