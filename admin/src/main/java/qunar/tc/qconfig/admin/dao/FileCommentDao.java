package qunar.tc.qconfig.admin.dao;

import java.util.Map;

public interface FileCommentDao {

    int insertOrUpdate(String group, String profile, String dataId, long version, String description);

    String query(String group, String profile, String dataId, long version);

    Map<Long, String> query(String group, String profile, String dataId);

    Map<Long, String> query(String group, String profile, String dataId, long startVersion, long endVersion);

    int delete(String group, String profile, String dataId);
}
