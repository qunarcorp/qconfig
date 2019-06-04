package qunar.tc.qconfig.admin.dao;

import java.util.List;
import java.util.Set;

import qunar.tc.qconfig.admin.model.PropertiesEntry;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author keli.wang
 */
public interface PropertiesEntryDao {

    void insert(final PropertiesEntry entry);

    int update(final PropertiesEntry entry, final long oldVersion);

    List<PropertiesEntry> selectByConfigMeta(final ConfigMeta configMeta);

    List<PropertiesEntry> selectByRef(final String key, final Reference ref);

    List<PropertiesEntry> select(String key,
                                 Set<String> groups,
                                 String profile,
                                 int pageNo,
                                 int pageSize);

    int selectCount(String key, Set<String> groups, String profile);

    int delete(final PropertiesEntry entry, final long newVersion);
}
