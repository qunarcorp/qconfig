package qunar.tc.qconfig.server.dao;

import qunar.tc.qconfig.server.exception.ChecksumFailedException;
import qunar.tc.qconfig.server.exception.FileDaoProcessException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-7
 * Time: 下午12:37
 */
public interface FileConfigDao {

    boolean delete(ConfigMeta meta);

    void save(VersionData<ConfigMeta> configId, ChecksumData<String> config) throws FileDaoProcessException;

    ChecksumData<String> find(VersionData<ConfigMeta> configId) throws FileDaoProcessException, ChecksumFailedException;
}
