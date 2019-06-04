package qunar.tc.qconfig.admin.service.impl;


import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.PropertiesEntryDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.PropertiesEntry;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.PropertiesEntryService;
import qunar.tc.qconfig.admin.support.PropertiesEntryUtil;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author keli.wang
 */
@Service
public class PropertiesEntryServiceImpl implements PropertiesEntryService {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesEntryServiceImpl.class);

    // 用来判断两个PropertiesEntry的(key, group_id, profile, data_id)元组相同
    private static final Equivalence<PropertiesEntry> ENTRY_UNIQE_KEY_EQUIVALENCE =
            new Equivalence<PropertiesEntry>() {
                @Override
                protected boolean doEquivalent(PropertiesEntry a, PropertiesEntry b) {
                    if (a.equals(b)) {
                        return true;
                    }
                    if (a == null || b == null || a.getClass() != b.getClass()) {
                        return false;
                    }
                    return Objects.equal(a.getKey(), b.getKey()) &&
                            Objects.equal(a.getGroupId(), b.getGroupId()) &&
                            Objects.equal(a.getProfile(), b.getProfile()) &&
                            Objects.equal(a.getDataId(), b.getDataId());
                }

                @Override
                protected int doHash(PropertiesEntry entry) {
                    return Objects.hashCode(entry.getKey(),
                                            entry.getGroupId(),
                                            entry.getProfile(),
                                            entry.getDataId());
                }
            };

    @Resource
    private PropertiesEntryDao propertiesEntryDao;

    @Resource
    private ReferenceDao referenceDao;

    private Map<String, PropertiesEntry> entriesToMap(final List<PropertiesEntry> entries) {
        final Map<String, PropertiesEntry> entryMap = Maps.newHashMap();
        for (PropertiesEntry entry : entries) {
            entryMap.put(entry.getKey(), entry);
        }
        return entryMap;
    }

    @Override
    @Transactional
    public void saveEntries(final CandidateSnapshot snapshot) throws ModifiedException {
        LOG.info("save properties entries in config: {}", snapshot);

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            final ConfigMeta configMeta = new ConfigMeta(snapshot.getGroup(),
                                                         snapshot.getDataId(),
                                                         snapshot.getProfile());
            final List<PropertiesEntry> oldEntries = propertiesEntryDao.selectByConfigMeta(configMeta);
            // 数据已被修改，不覆盖新数据
            if (!oldEntries.isEmpty() && oldEntries.get(0).getVersion() >= snapshot.getEditVersion()) {
                return;
            }

            final Map<String, PropertiesEntry> oldEntryMap = entriesToMap(oldEntries);
            final Map<String, PropertiesEntry> entryMap = entriesToMap(PropertiesEntryUtil.toEntries(snapshot));

            MapDifference<String, PropertiesEntry> difference = Maps.difference(oldEntryMap, entryMap, ENTRY_UNIQE_KEY_EQUIVALENCE);

            // 删除已经从配置文件中删除的配置项
            for (PropertiesEntry entry : difference.entriesOnlyOnLeft().values()) {
                if (propertiesEntryDao.delete(entry, snapshot.getEditVersion()) != 1) {
                    Monitor.SAVE_ENTRIES_FAILED_COUNT.inc();
                    throw new ModifiedException();
                }
            }

            // 更新配置文件中未被删除的配置项
            for (String key : difference.entriesInCommon().keySet()) {
                final PropertiesEntry entry = entryMap.get(key);
                final long oldVersion = oldEntryMap.get(key).getVersion();

                if (propertiesEntryDao.update(entry, oldVersion) != 1) {
                    Monitor.SAVE_ENTRIES_FAILED_COUNT.inc();
                    throw new ModifiedException();
                }
            }

            // 插入新增加的配置项
            for (PropertiesEntry entry : difference.entriesOnlyOnRight().values()) {
                try {
                    propertiesEntryDao.insert(entry);
                } catch (DuplicateKeyException e) {
                    LOG.warn("insert new PropertiesEntry failed. entry={}", entry, e);
                    Monitor.SAVE_ENTRIES_FAILED_COUNT.inc();
                    throw new ModifiedException();
                }
            }
        } finally {
            Monitor.SAVE_ENTRIES_TIMER.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @Transactional
    public void removeEntries(final CandidateSnapshot snapshot) throws ModifiedException {
        LOG.info("remove properties entries in config: {}", snapshot);

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            // 删除时Config的version不变，但是其candidate的version会加1
            final long deletedEditVersion = snapshot.getEditVersion() + 1;
            final ConfigMeta configMeta = new ConfigMeta(snapshot.getGroup(),
                                                         snapshot.getDataId(),
                                                         snapshot.getProfile());
            final List<PropertiesEntry> oldEntries = propertiesEntryDao.selectByConfigMeta(configMeta);
            // 没有需要删除的索引条目
            if (oldEntries.isEmpty()) {
                return;
            }
            // 索引条目已经被修改，不必重复修改
            if (oldEntries.get(0).getVersion() >= deletedEditVersion) {
                return;
            }

            for (PropertiesEntry entry : oldEntries) {
                // 逐个删除索引条目，删除失败说明数据可能同时被其它地方修改了，此时抛出异常
                if (propertiesEntryDao.delete(entry, deletedEditVersion) != 1) {
                    Monitor.REMOVE_ENTRIES_FAILED_COUNT.inc();
                    throw new ModifiedException();
                }
            }
        } finally {
            Monitor.REMOVE_ENTRIES_TIMER.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @Transactional
    public void handleSnapshotByStatus(final CandidateSnapshot snapshot) throws ModifiedException {
        LOG.info("handle snapshot, snapshot={}", snapshot);

        if (snapshot.getStatus() == StatusType.PUBLISH) {
            saveEntries(snapshot);
        } else if (snapshot.getStatus() == StatusType.DELETE) {
            removeEntries(snapshot);
        }
    }

    @Override
    public List<PropertiesEntry> searchEntries(String key,
                                               Set<String> groups,
                                               String profile,
                                               int pageNo,
                                               int pageSize) {
        LOG.info("search properties entries with key={}, groups={}, profile={}, pageNo={}, pageSize={}",
                 key, groups, profile, pageNo, pageSize);

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return propertiesEntryDao.select(key, groups, profile, pageNo, pageSize);
        } finally {
            Monitor.SEARCH_ENTRIES_TIMER.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int countEntries(String key, Set<String> groups, String profile) {
        LOG.info("search properties entries with key={}, groups={}, profile={}", key, groups, profile);
        return propertiesEntryDao.selectCount(key, groups, profile);
    }

    @Override
    public List<PropertiesEntry> searchRefEntries(String key, Set<String> groups, String profile) {
        LOG.info("search reference properties entries with key={}, groups={}, profile={}",
                 key, groups, profile);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            final List<Reference> references = referenceDao.searchReferences(groups, profile);
            List<PropertiesEntry> entries = Lists.newArrayList();
            for (Reference reference : references) {
                if (FileChecker.isPropertiesFile(reference.getRefDataId())) {
                    entries.addAll(propertiesEntryDao.selectByRef(key, reference));
                }
            }

            return entries;
        } finally {
            Monitor.SEARCH_REF_ENTRIES_TIMER.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
