package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ConfigEditorSettingsDao;
import qunar.tc.qconfig.admin.model.ConfigEditorSettings;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;

import javax.annotation.Resource;

/**
 * @author keli.wang
 * @since 2017/5/15
 */
@Service
public class ConfigEditorSettingsServiceImpl implements ConfigEditorSettingsService {

    @Resource
    private ConfigEditorSettingsDao configEditorSettingsDao;

    @Override
    public int updateUseAdvancedEditor(final String groupId, final String dataId, final boolean useAdvancedEditor) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "groupId不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataId), "dataId不能为空");

        return configEditorSettingsDao.saveOrUpdate(groupId, dataId, new ConfigEditorSettings(useAdvancedEditor));
    }

    @Override
    public ConfigEditorSettings settingsOf(final String groupId, final String dataId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "groupId不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataId), "dataId不能为空");
        try {
            return configEditorSettingsDao.query(groupId, dataId);
        } catch (EmptyResultDataAccessException ignore) {
            return ConfigEditorSettings.DEFAULT;
        }
    }
}
