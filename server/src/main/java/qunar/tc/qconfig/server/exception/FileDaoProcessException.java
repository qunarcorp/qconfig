package qunar.tc.qconfig.server.exception;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 11:54 AM
 */
public class FileDaoProcessException extends Exception {
    private static final long serialVersionUID = 1771137937219205379L;
    private final VersionData<ConfigMeta> configId;

    public FileDaoProcessException(VersionData<ConfigMeta> configId, String message) {
        super(message);
        this.configId = configId;
    }

    public VersionData<ConfigMeta> getConfigId() {
        return configId;
    }
}
