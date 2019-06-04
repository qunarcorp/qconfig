package qunar.tc.qconfig.admin.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileTemplateDao;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateMeta;
import qunar.tc.qconfig.admin.model.TemplateType;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.support.SQLUtil;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/1/28 14:54
 */
@Repository
public class FileTemplateDaoImpl implements FileTemplateDao {

    private static final ResultSetExtractor<String> DETAIL_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("detail");
            }
            return null;
        }
    };
    private static final ResultSetExtractor<Integer> VERSION_EXTRACTOR = new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getInt("version");
            }
            return AdminConstants.TEMPLATE_VERSION_NOT_EXIST;
        }
    };
    private static final RowMapper<TemplateMeta> TEMPLATE_META_MAPPER = new RowMapper<TemplateMeta>() {
        @Override
        public TemplateMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TemplateMeta(
                    rs.getString("group_id"), rs.getString("template"),
                    rs.getString("description"), TemplateType.fromCode(rs.getInt("type")));
        }
    };
    private static final RowMapper<TemplateInfo> TEMPLATE_INFO_MAPPER = new RowMapper<TemplateInfo>() {
        @Override
        public TemplateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TemplateInfo(rs.getString("group_id"), rs.getString("template"),
                    TemplateType.fromCode(rs.getInt("type")),
                    rs.getString("detail"), rs.getString("description"),
                    rs.getInt("version"), rs.getTimestamp("update_time"));
        }
    };

    private static final ResultSetExtractor<TemplateInfo> TEMPLATE_INFO_MAPPER_SINGLE = new ResultSetExtractor<TemplateInfo>() {
        @Override
        public TemplateInfo extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new TemplateInfo(rs.getString("group_id"), rs.getString("template"),
                        TemplateType.fromCode(rs.getInt("type")),
                        rs.getString("detail"), rs.getString("description"),
                        rs.getInt("version"), rs.getTimestamp("update_time"));
            } else {
                return null;
            }
        }
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public TemplateInfo selectTemplateInfo(String group, String template) {
        return jdbcTemplate.query("SELECT group_id, template, type, description, detail, version, update_time FROM file_template WHERE group_id=? AND template=?", TEMPLATE_INFO_MAPPER_SINGLE, group, template);
    }

    @Override
    public String selectTemplateDetail(String group, String template) {
        return jdbcTemplate.query("SELECT detail FROM file_template WHERE group_id=? AND template=?", DETAIL_EXTRACTOR, group, template);
    }

    @Override
    public Integer selectVersion(String group, String template) {
        return jdbcTemplate.query("SELECT version FROM file_template WHERE group_id=? AND template=?", VERSION_EXTRACTOR, group, template);
    }

    @Override
    public void setTemplate(String group, String template, TemplateType type, String description, String detail, String operator) {
        jdbcTemplate.update("INSERT INTO file_template(group_id,template,type,description,detail,operator,create_time) " +
                        "VALUES(?,?,?,?,?,?,now()) ON DUPLICATE KEY UPDATE description=?,detail=?,operator=?,version=version+1",
                group, template, type.getCode(), description, detail, operator, description, detail, operator);
    }

    @Override
    public List<TemplateMeta> selectTemplates(Collection<String> groups) {
        return jdbcTemplate.query("SELECT group_id,template,description,type FROM file_template WHERE group_id in " + SQLUtil.generateStubs(groups.size()), TEMPLATE_META_MAPPER, groups.toArray());
    }

    @Override
    public List<TemplateInfo> queryTemplateInfoByGroup(String group) {
        return jdbcTemplate.query("SELECT group_id, template, type, description, detail, version, update_time FROM file_template WHERE group_id=?", TEMPLATE_INFO_MAPPER, group);
    }

    @Override
    public List<TemplateInfo> queryTemplateInfo(Collection<String> groups) {
        return jdbcTemplate.query("SELECT group_id, template, type, description, detail, version, update_time FROM file_template WHERE group_id in " + SQLUtil.generateStubs(groups.size()), TEMPLATE_INFO_MAPPER, groups.toArray());
    }
}
