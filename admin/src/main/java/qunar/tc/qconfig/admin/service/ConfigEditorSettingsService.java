package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.ConfigEditorSettings;

/**
 * @author keli.wang
 * @since 2017/5/15
 */
public interface ConfigEditorSettingsService {
    int updateUseAdvancedEditor(final String groupId, final String dataId, final boolean useAdvancedEditor);

    ConfigEditorSettings settingsOf(final String groupId, final String dataId);
}
