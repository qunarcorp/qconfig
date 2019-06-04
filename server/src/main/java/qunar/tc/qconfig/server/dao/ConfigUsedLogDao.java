package qunar.tc.qconfig.server.dao;

import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.server.bean.LogEntry;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 14:51
 */
public interface ConfigUsedLogDao {

    void updateRemarks(ConfigMeta realMeta, ConfigMeta sourceMeta, String profile, String ip, int port, long version, ConfigUsedType type, String remarks);

    void update(ConfigMeta realMeta, ConfigMeta sourceMeta, String profile, String ip, int port, long version, ConfigUsedType type, String remarks);

    void delete(ConfigMeta sourceMeta, String ip, int port);

    void batchSave(List<LogEntry> logEntries);
}
