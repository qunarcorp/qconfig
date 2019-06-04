package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.service.CheckKeyConflictService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.io.StringReader;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * @author zhenyu.nie created on 2015 2015/10/21 16:21
 */
@Service
public class CheckKeyConflictServiceImpl implements CheckKeyConflictService {

    private static final Logger logger = LoggerFactory.getLogger(CheckKeyConflictServiceImpl.class);

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private ConfigDao configDao;

    @Resource
    private UserContextService userContextService;

    private final Predicate<CandidateSnapshot> isPropertyFile = new Predicate<CandidateSnapshot>() {
        @Override
        public boolean apply(CandidateSnapshot input) {
            return needCheck(input.getDataId());
        }
    };

    @Override
    public boolean needCheck(String dataId) {
        return dataId.endsWith(AdminConstants.PROPERTY_FILE_SUFFIX);
    }

    @Override
    public Multimap<String, ConfigMeta> checkKeyConflict(CandidateSnapshot snapshot) {
        if (!needCheck(snapshot.getDataId()) || snapshot.getData().isEmpty()) {
            return ImmutableMultimap.of();
        }

        List<CandidateSnapshot> toCheckedSnapshots = findToCheckedCandidate(snapshot);
        return checkConflict(snapshot, toCheckedSnapshots);
    }

    private List<CandidateSnapshot> findToCheckedCandidate(final CandidateSnapshot snapshot) {
        List<CandidateSnapshot> affectedCandidates = findAffectedCandidate(snapshot);

        // profile -> dataId -> snapshot
        ImmutableTable.Builder<String, String, CandidateSnapshot> builder = ImmutableTable.builder();
        for (CandidateSnapshot affectedCandidate : affectedCandidates) {
            builder.put(affectedCandidate.getProfile(), affectedCandidate.getDataId(), affectedCandidate);
        }
        Table<String, String, CandidateSnapshot> candidateTable = builder.build();

        Collection<CandidateSnapshot> lowerLevelCandidates = findAffectedLowerLevelCandidates(snapshot, candidateTable);
        Collection<CandidateSnapshot> greaterOrEqualLevelCandidates = findAffectedGreaterOrEqualLevelCandidates(snapshot, candidateTable);

        return Lists.newArrayList(Iterables.concat(lowerLevelCandidates, greaterOrEqualLevelCandidates));
    }

    private ImmutableList<CandidateSnapshot> findAffectedCandidate(final CandidateSnapshot snapshot) {
        return ImmutableList.copyOf(StreamSupport.stream(Iterables.concat(referenceDao.findCurrentSnapshotsInGroup(snapshot.getGroup()),
                configDao.findCurrentSnapshotsInGroup(snapshot.getGroup())).spliterator(), false)
                .filter(isPropertyFile)
                .filter((Predicate<CandidateSnapshot>) input -> !isSameFile(snapshot, input))
                .filter((Predicate<CandidateSnapshot>) input ->
                        Environment.hasAffect(Environment.fromProfile(snapshot.getProfile()), Environment.fromProfile(input.getProfile())))
                .iterator());
    }

    private Multimap<String, ConfigMeta> checkConflict(CandidateSnapshot snapshot, List<CandidateSnapshot> toCheckedSnapshots) {
        Properties p = loadProperties(snapshot);
        Multimap<String, ConfigMeta> conflicts = HashMultimap.create();
        for (CandidateSnapshot toCheckedSnapshot : toCheckedSnapshots) {
            ConfigMeta meta = new ConfigMeta(toCheckedSnapshot.getGroup(), toCheckedSnapshot.getDataId(), toCheckedSnapshot.getProfile());
            Properties toCheckProperties = loadProperties(toCheckedSnapshot);
            for (Object key : toCheckProperties.keySet()) {
                if (p.containsKey(key)) {
                    conflicts.put((String) key, meta);
                }
            }
        }
        return conflicts;
    }

    private Properties loadProperties(CandidateSnapshot snapshot) {
        Properties p = new Properties();
        try {
            p.load(new StringReader(snapshot.getData()));
        } catch (Exception e) {
            logger.error("load properties error, {}", snapshot);
            throw new RuntimeException("load properties error, " + snapshot, e);
        }
        return p;
    }

    private Collection<CandidateSnapshot> findAffectedLowerLevelCandidates(CandidateSnapshot snapshot, Table<String, String, CandidateSnapshot> candidateTable) {
        List<CandidateSnapshot> results = Lists.newArrayList();
        for (String level : findLowerLevels(snapshot.getGroup(), snapshot.getProfile(), candidateTable.rowKeySet())) {
            findAffectedLowerLevelCandidates(snapshot, level, candidateTable, results);
        }
        return results;
    }

    private void findAffectedLowerLevelCandidates(CandidateSnapshot snapshot, String level, Table<String, String, CandidateSnapshot> candidateTable, List<CandidateSnapshot> results) {
        CandidateSnapshot sameNameFile = candidateTable.get(level, snapshot.getDataId());
        if (sameNameFile != null) {
            return;
        }

        results.addAll(candidateTable.row(level).values());
        for (String lowerLevel : findLowerLevels(snapshot.getGroup(), level, candidateTable.rowKeySet())) {
            findAffectedLowerLevelCandidates(snapshot, lowerLevel, candidateTable, results);
        }
    }

    private Collection<CandidateSnapshot> findAffectedGreaterOrEqualLevelCandidates(CandidateSnapshot snapshot, Table<String, String, CandidateSnapshot> candidateTable) {
        Map<String, CandidateSnapshot> results = Maps.newHashMap();
        findAffectedGreaterOrEqualLevelCandidates(snapshot, snapshot.getProfile(), candidateTable, results);
        return results.values();
    }

    private void findAffectedGreaterOrEqualLevelCandidates(CandidateSnapshot snapshot, String level, Table<String, String, CandidateSnapshot> candidateTable, Map<String, CandidateSnapshot> results) {
        for (CandidateSnapshot lteLevelSnapshot : candidateTable.row(level).values()) {
            if (results.get(lteLevelSnapshot.getDataId()) == null && !snapshot.getDataId().equals(lteLevelSnapshot.getDataId())) {
                results.put(lteLevelSnapshot.getDataId(), lteLevelSnapshot);
            }
        }

        Optional<String> greaterLevel = findGreaterLevel(level);
        greaterLevel.ifPresent(greaterLevelValue -> findAffectedGreaterOrEqualLevelCandidates(snapshot, greaterLevelValue, candidateTable, results));
    }

    private boolean isSameFile(CandidateSnapshot lhs, CandidateSnapshot rhs) {
        return lhs.getGroup().equals(rhs.getGroup())
                && lhs.getProfile().equals(rhs.getProfile())
                && lhs.getDataId().equals(rhs.getDataId());
    }

    private Collection<String> findLowerLevels(String group, String profile, Collection<String> levels) {
        Set<String> results = Sets.newHashSet();
        Environment environment = Environment.fromProfile(profile);
        if (environment.isResources()) {
            Set<String> resultSet = Sets.newHashSet(userContextService.getProfiles(group));
            resultSet.remove(Environment.RESOURCES.profile());
            return resultSet;
        } else if (environment.defaultProfile().equals(profile)) {
            for (String level : levels) {
                if (level.startsWith(profile) && level.length() > profile.length()) {
                    results.add(level);
                }
            }
        }
        return results;
    }

    private Optional<String> findGreaterLevel(String profile) {
        Environment environment = Environment.fromProfile(profile);
        if (environment.isResources()) {
            return Optional.empty();
        } else if (environment.defaultProfile().equals(profile)) {
            return Optional.of(Environment.RESOURCES.defaultProfile());
        } else {
            return Optional.of(environment.defaultProfile());
        }
    }
}
