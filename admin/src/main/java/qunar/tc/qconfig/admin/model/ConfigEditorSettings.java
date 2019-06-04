package qunar.tc.qconfig.admin.model;

import java.io.Serializable;

/**
 * @author keli.wang
 * @since 2017/5/15
 */
public class ConfigEditorSettings implements Serializable {

    public static final ConfigEditorSettings DEFAULT = new ConfigEditorSettings(true);

    private final boolean useAdvancedEditor;

    public ConfigEditorSettings(boolean useAdvancedEditor) {
        this.useAdvancedEditor = useAdvancedEditor;
    }

    public boolean isUseAdvancedEditor() {
        return useAdvancedEditor;
    }
}
