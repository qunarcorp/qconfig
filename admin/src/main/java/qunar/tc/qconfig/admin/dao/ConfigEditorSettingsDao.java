package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.ConfigEditorSettings;

/**
 * @author keli.wang
 * @since 2017/5/15
 */
public interface ConfigEditorSettingsDao {

    int saveOrUpdate(final String groupId, final String dataId, final ConfigEditorSettings settings);

    ConfigEditorSettings query(final String groupId, final String dataId);
}
