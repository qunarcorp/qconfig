package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.service.CandidateLevelService;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.Environment;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class CandidateLevelServiceImpl implements CandidateLevelService {

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private ConfigDao configDao;

    @Override
    public Map<String, CandidateSnapshot> findUpperAndSameLevelFiles(List<CandidateSnapshot> filesInGroup, String profile) {
        Map<String, CandidateSnapshot> results = Maps.newHashMap();
        Set<String> upperLevelProfiles = listUpperLevelsWithSelf(profile);
        for (CandidateSnapshot candidate : filesInGroup) {
            if (!upperLevelProfiles.contains(candidate.getProfile())) {
                continue;
            }
            String dataId = candidate.getDataId();
            if (results.containsKey(dataId)) {
                if (isLowerLevelProfile(candidate.getProfile(), results.get(dataId).getProfile())) {
                    results.put(dataId, candidate);
                }
            } else {
                results.put(candidate.getDataId(), candidate);
            }
        }
        return results;
    }

    @Override
    public List<CandidateSnapshot> findCandidates(String group) {
        Stream<CandidateSnapshot> candidates = StreamSupport.stream(Iterables.concat(
                configDao.findCurrentSnapshotsInGroup(group), referenceDao.findCurrentSnapshotsInGroup(group)).spliterator(), false);
        return candidates.collect(Collectors.toList());
    }

    private boolean isLowerLevelProfile(String targetProfile, String baseProfile) {
        Environment baseEnv = Environment.fromProfile(baseProfile);
        Environment targetEnv = Environment.fromProfile(targetProfile);
        if (!Strings.isNullOrEmpty(targetEnv.subEnv()) && baseProfile.equals(targetEnv.defaultProfile())) {
            return true;
        }
        return baseEnv.isResources() && !targetEnv.isResources();
    }

    private Optional<String> findUpperLevel(String profile) {
        Environment environment = Environment.fromProfile(profile);
        if (environment.isResources()) {
            return Optional.empty();
        } else if (environment.defaultProfile().equals(profile)) {
            return Optional.of(Environment.RESOURCES.defaultProfile());
        } else {
            return Optional.of(environment.defaultProfile());
        }
    }

    private Set<String> listUpperLevelsWithSelf(String profile) {
        Set<String> upperLevels = Sets.newHashSet();
        upperLevels.add(profile);
        Optional<String> greaterLevel = findUpperLevel(profile);
        while (greaterLevel.isPresent()) {
            upperLevels.add(greaterLevel.get());
            greaterLevel = findUpperLevel(greaterLevel.get());
        }
        return upperLevels;
    }
}
