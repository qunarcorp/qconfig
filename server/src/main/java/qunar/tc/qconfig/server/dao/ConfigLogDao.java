package qunar.tc.qconfig.server.dao;

import qunar.tc.qconfig.server.bean.LogEntry;

import java.util.List;

/**
 * User: zhaohuiyu
 * Date: 5/21/14
 * Time: 4:58 PM
 */
public interface ConfigLogDao {
    void batchSave(List<LogEntry> logEntries);
}
