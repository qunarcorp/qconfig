package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.cloud.vo.TemplateMetaWithName;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.IllegalTemplateException;
import qunar.tc.qconfig.admin.model.TemplateInfo;
import qunar.tc.qconfig.admin.model.TemplateType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;

import java.util.Optional;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/1/27 14:24
 */
public interface FileTemplateService {

    Optional<Map.Entry<String, String>> getTemplate(String group, String dataId);

    void setTemplate(String group, String dataId, String templateGroup, String template);

    void setTemplate(String group, String dataId, String profile, int dataVersion, String templateGroup, String template, int templateVersion);

    Optional<TemplateInfo> getTemplateInfo(String group, String template);


    /**
     * 由于前端问题，无法在不对数据爆炸的情况下实现模版对应文件名限制，
     * 所以对所有对模版内容对请求添加了包装操作，这里是返回含有包装对模版内容
     *
     * @param group 模版group
     * @param template 模版名称
     * @return 没有解开包装对模版
     */
    Optional<TemplateInfo> getTemplateInfoWithoutConvert(String group, String template);

    Optional<String> getTemplateDetail(String group, String template);

    Optional<String> processTemplateValue(CandidateDTO dto) throws IllegalTemplateException;

    void checkPropertiesTemplateValue(CandidateDTO dto);

    void checkPropertiesTemplate(CandidateDTO dto);

    Optional<String> getPropertiesTemplate(ConfigMeta meta);

    void setPropertiesTemplate(CandidateDTO dto);

    void setDefaultConfigId(ConfigMeta meta, long configId);

    void deleteDefaultConfigId(ConfigMeta meta);

    long getDefaultConfigId(ConfigMeta meta);

    Optional<String> getTemplateDetailWithDefaultConfigId(String group, String template, long defaultConfigId);

    Optional<TemplateInfo> getTemplateInfo(String group, String template, int version);

    Optional<TemplateInfo> getTemplateInfoWithoutConvert(String group, String template, int version);

    Optional<String> getTemplateDetail(String group, String template, int version);

    Optional<String> getTemplateDetailWithDefaultConfigId(String group, String template, int version, long defaultConfigId);

    Optional<String> getTemplateDetailByFile(String group, String dataId);

    void saveTemplateDetail(String group, String template, TemplateType type, String description, String detail);

    List<TemplateInfo> getTemplateInfoWithoutPropertiesByGroup(String group);

    Map<String, List<TemplateMetaWithName>> getTemplatesWithDefaultWithoutProperties(Set<String> userGroups);

    List<TemplateInfo> getTemplateInfoListHistory(String group, String template);

    long saveDefaultTemplateConfig(String defaultConfig);

    /**
     * 获取对应版本文件映射的模版版本
     * @param group 文件group
     * @param dataId 文件dataId
     * @param dataVersion 文件Version
     * @return 模版Version
     */
    int getFileMappingTemplateCurrentVersion(String group, String dataId,String profile, long dataVersion);

    int setFileTemplateMapping(String group, String dataId, String profile, int dataVersion, String templateGroup, String template, int templateVersion);

    Optional<TemplateInfo> getTemplateInfoByFile(String group, String dateId, String profile, long dataVersion);

    int getNewestTemplateVersion(String group, String template);

    void completeDelete(ConfigMeta meta);
}
