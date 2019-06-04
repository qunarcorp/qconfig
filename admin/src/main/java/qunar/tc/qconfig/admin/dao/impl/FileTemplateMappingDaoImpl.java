package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.FileTemplateMappingDao;
import qunar.tc.qconfig.admin.dto.TemplateMappingDto;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/1/27 14:14
 */
@Repository
public class FileTemplateMappingDaoImpl implements FileTemplateMappingDao {

    //模版版本查询
    private static final String TEMPLATE_VERSION_SELECT_SQL = "SELECT template_version FROM file_template_version_mapping WHERE group_id = ? AND profile=? AND data_id = ? AND version = ?";

    private static final String TEMPLATE_VERSION_SELECT_SQL_OLD = "SELECT template_version FROM file_template_version_mapping WHERE group_id = ? AND profile=? AND data_id = ? ORDER BY id ASC LIMIT 1";


    //模版文件映射名称查询
    private static final String TEMPLATE_NAME_SELECT_SQL = "SELECT template_group, template FROM file_template_version_mapping WHERE group_id=? AND data_id=?";

    private static final String TEMPLATE_NAME_SELECT_SQL_OLD = "SELECT template_group, template FROM file_template_mapping WHERE group_id=? AND data_id=?";

    //模版映射新增
    private static final String TEMPLATE_MAPPING_INSERT = "INSERT INTO file_template_version_mapping(group_id, data_id, template_group, template, create_time) VALUES(?, ?, ?, ?, now())";

    //模版映射新增
    private static final String TEMPLATE_VERSION_MAPPING_INSERT = "INSERT INTO file_template_version_mapping(group_id, data_id, profile, version, template_group, template, template_version, create_time) VALUES(?, ?, ? ,?, ?, ?, ?, now())";

    private static final String TEMPLATE_DELETE = "DELETE FROM file_template_version_mapping WHERE group_id=? AND profile=? AND data_id=?";

    private static final String TEMPLATE_DELETE_OLD = "DELETE FROM file_template_mapping WHERE group_id=? AND data_id=?";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map.Entry<String, String> selectTemplate(String group, String dataId) {
        Map.Entry<String, String> result =  jdbcTemplate.query(TEMPLATE_NAME_SELECT_SQL, TEMPLATE_EXTRACTOR, group, dataId);
        if (result == null) {
            return jdbcTemplate.query(TEMPLATE_NAME_SELECT_SQL_OLD, TEMPLATE_EXTRACTOR, group, dataId);
        }
        return result;
    }

    @Override
    public void setTemplate(String group, String dataId, String templateGroup, String template) {
        jdbcTemplate.update(TEMPLATE_MAPPING_INSERT, group, dataId, templateGroup, template);
    }


    @Override
    public int setTemplateWithVersion(String group, String dataId, String profile, String templateGroup, String template, int dataVersion, int templateVersion) {
        return jdbcTemplate.update(TEMPLATE_VERSION_MAPPING_INSERT, group, dataId, profile, dataVersion, templateGroup, template, templateVersion);
    }


    @Override
    public int selectTemplateVersion(String group, String dataId, String profile, long dataVersion) {
        return jdbcTemplate.query(TEMPLATE_VERSION_SELECT_SQL, new Object[] { group, profile, dataId, dataVersion },
                TEMPLATE_VERSION);
    }

    @Override
    public int selectOldTemplateVersion(String group, String dataId, String profile) {
        return jdbcTemplate.query(TEMPLATE_VERSION_SELECT_SQL_OLD, new Object[] { group, profile, dataId },
                TEMPLATE_VERSION);
    }

    @Override
    public void completeDelete(String group, String dataId, String profile) {
        jdbcTemplate.update(TEMPLATE_DELETE, group, profile, dataId);
        jdbcTemplate.update(TEMPLATE_DELETE_OLD, group, dataId);
    }

    private static final ResultSetExtractor<Map.Entry<String, String>> TEMPLATE_EXTRACTOR = new ResultSetExtractor<Map.Entry<String, String>>() {
        @Override
        public Map.Entry<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return Maps.immutableEntry(rs.getString("template_group"), rs.getString("template"));
            }
            return null;
        }
    };

    private static final ResultSetExtractor<Integer> TEMPLATE_VERSION = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("template_version");
            }
            return AdminConstants.TEMPLATE_MAPPING_VERSION_NOT_EXIST;
        }
    };
}
