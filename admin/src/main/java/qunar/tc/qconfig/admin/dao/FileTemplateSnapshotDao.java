package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateType;

import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/4/19 14:30
 */
public interface FileTemplateSnapshotDao {

    String selectTemplateDetail(String group, String template, int version);

    void insertTemplate(String group, String template, TemplateType type, String description, String detail, String operator, int version);

    TemplateInfo selectTemplateInfo(String group, String template, int version);

    List<TemplateInfo> selectTemplateInfo(String group, String template);
}
