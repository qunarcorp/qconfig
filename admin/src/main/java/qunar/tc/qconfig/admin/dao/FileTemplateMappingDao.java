package qunar.tc.qconfig.admin.dao;

import com.google.common.collect.ImmutableList;
import qunar.tc.qconfig.admin.dto.TemplateMappingDto;

import java.util.Map;

/**
 * @author zhenyu.nie created on 2016 2016/1/27 14:13
 */
public interface FileTemplateMappingDao {

    Map.Entry<String, String> selectTemplate(String group, String dataId);

    void setTemplate(String group, String dataId, String templateGroup, String template);

    int setTemplateWithVersion(String group, String dataId, String dataProfile, String templateGroup, String template, int dataVersion, int templateVersion);

    int selectTemplateVersion(String group, String dataId, String profile, long dataVersion);

    int selectOldTemplateVersion(String group, String dataId, String profile);

    void completeDelete(String group, String dataId, String profile);
}
