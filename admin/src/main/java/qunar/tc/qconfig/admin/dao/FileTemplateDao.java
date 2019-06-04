package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateMeta;
import qunar.tc.qconfig.admin.model.TemplateType;

import java.util.Collection;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/1/28 14:53
 */
public interface FileTemplateDao {

    TemplateInfo selectTemplateInfo(String group, String template);

    String selectTemplateDetail(String group, String template);

    Integer selectVersion(String group, String template);

    void setTemplate(String group, String template, TemplateType type, String description, String detail, String operator);

    List<TemplateMeta> selectTemplates(Collection<String> groups);

    List<TemplateInfo> queryTemplateInfoByGroup(String group);

    List<TemplateInfo> queryTemplateInfo(Collection<String> groups);
}
