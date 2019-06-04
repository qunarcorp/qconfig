package qunar.tc.qconfig.server.config;

import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-7
 * Time: 下午12:39
 */
public interface ConfigStore {

    ChecksumData<String> findConfig(VersionData<ConfigMeta> configId) throws ConfigNotFoundException;

    VersionData<ChecksumData<String>> forceLoad(String ip, VersionData<ConfigMeta> configId) throws ConfigNotFoundException;

    void update(ConfigMeta configMeta);

    List<VersionData<ConfigMeta>> loadByGroupAndProfile(String group, String profile);
}
