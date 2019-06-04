package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.PropertiesEntryDiff;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Set;

public interface PropertiesEntryLogDao {

    void insert(final PropertiesEntryDiff entry);

    void batchInsert(final List<PropertiesEntryDiff> entries);

    void delete(ConfigMeta meta);

    void delete(PropertiesEntryDiff entry);

    List<PropertiesEntryDiff> selectByConfigMeta(final ConfigMeta configMeta);

    List<Long> selectLatestIdsOfDistinctKeys(Set<String> groups, String profile, String dataId, String key,
                                             String profileLike, String dataIdLike, String keyLike, int page, int pageSize);

    List<PropertiesEntryDiff> selectByIds(List<Long> ids);

    int countDistinctKeys(Set<String> groups, String profile, String dataId, String key, String profileLike, String dataIdLike, String keyLike);

    List<PropertiesEntryDiff> selectKey(ConfigMeta meta, String key, int page, int pageSize);

    int countKey(ConfigMeta meta, String key);

    boolean isMetaLogExist(ConfigMeta meta);
}
