package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.FileTemplateSnapshotDao;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateType;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 14:32
 */
@Repository
public class FileTemplateSnapshotDaoImpl implements FileTemplateSnapshotDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public TemplateInfo selectTemplateInfo(String group, String template, int version) {
        return jdbcTemplate.query("SELECT group_id, template, type, description, detail, version, update_time FROM file_template_snapshot WHERE group_id=? AND template=? AND version=?", INFO_EXTRACTOR, group, template, version);
    }

    @Override
    public List<TemplateInfo> selectTemplateInfo(String group, String template) {
        return jdbcTemplate.query("SELECT group_id, template, type, description, detail, version, update_time, operator FROM file_template_snapshot WHERE group_id=? AND template=?", INFO_LIST_EXTRACTOR, group, template);
    }

    @Override
    public String selectTemplateDetail(String group, String template, int version) {
        return jdbcTemplate.query("SELECT detail FROM file_template_snapshot WHERE group_id=? AND template=? AND version=?", DETAIL_EXTRACTOR, group, template, version);
    }

    @Override
    public void insertTemplate(String group, String template, TemplateType type, String description, String detail, String operator, int version) {
        jdbcTemplate.update("INSERT INTO file_template_snapshot(group_id, template, type, description, detail, operator, version, create_time) VALUES(?, ?, ?, ?, ?, ?, ?, now())", group, template, type.getCode(), description, detail, operator, version);
    }

    private static final ResultSetExtractor<String> DETAIL_EXTRACTOR = new ResultSetExtractor<String>() {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return rs.getString("detail");
            }
            return null;
        }
    };

    private static final ResultSetExtractor<TemplateInfo> INFO_EXTRACTOR = new ResultSetExtractor<TemplateInfo>() {
        @Override
        public TemplateInfo extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return new TemplateInfo(rs.getString("group_id"), rs.getString("template"),
                        TemplateType.fromCode(rs.getInt("type")),
                        rs.getString("detail"), rs.getString("description"),
                        rs.getInt("version"), rs.getTimestamp("update_time"));
            }
            return null;
        }
    };

    private static final ResultSetExtractor<List<TemplateInfo>> INFO_LIST_EXTRACTOR = new ResultSetExtractor<List<TemplateInfo>>() {
        @Override
        public List<TemplateInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<TemplateInfo> templateInfos = Lists.newLinkedList();
            while (rs.next()) {
                templateInfos.add(new TemplateInfo(rs.getString("group_id"), rs.getString("template"),
                        TemplateType.fromCode(rs.getInt("type")),
                        rs.getString("detail"), rs.getString("description"),
                        rs.getInt("version"), rs.getTimestamp("update_time"), rs.getString("operator")));
            }
            return templateInfos;
        }
    };
}
