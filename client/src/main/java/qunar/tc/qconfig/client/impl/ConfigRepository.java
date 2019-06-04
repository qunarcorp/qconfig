package qunar.tc.qconfig.client.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lepdou 2017-06-30
 */
class ConfigRepository {

    private static final ConfigRepository INSTANCE = new ConfigRepository();

    /**
     * (groupName,fileName) -> (fileVersion, fileData)
     */
    private ConcurrentMap<Meta, Map.Entry<Long, String>> repository;

    public static ConfigRepository getInstance() {
        return INSTANCE;
    }

    private ConfigRepository() {
        repository = Maps.newConcurrentMap();
    }

    void saveOrUpdate(Meta meta, long version, String fileData) {
        if (meta == null) {
            return;
        }
        String group = meta.getGroupName();
        String fileName = meta.getFileName();
        if (Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(fileName)) {
            return;
        }

        repository.put(meta, new AbstractMap.SimpleImmutableEntry(version, fileData));
    }

    Map<Meta, Map.Entry<Long, String>> getAllConfigs() {
        return repository;
    }

    String getData(Meta meta) {
        Map.Entry<Long, String> entry = repository.get(meta);
        if (entry == null) return "";

        return entry.getValue();
    }

    Set<Meta> allMetas() {
        return repository.keySet();
    }

}
