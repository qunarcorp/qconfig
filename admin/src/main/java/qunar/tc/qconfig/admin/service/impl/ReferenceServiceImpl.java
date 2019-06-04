package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dao.ReferenceLogDao;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.model.ReferenceInfo;
import qunar.tc.qconfig.admin.model.ReferenceLog;
import qunar.tc.qconfig.admin.model.RelativeReference;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.CheckEnvConflictService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.ReferenceService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Date: 14-6-27 Time: 下午3:11
 *
 * @author: xiao.liang
 * @description:
 */
@Service("referenceService")
public class ReferenceServiceImpl implements ReferenceService {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceServiceImpl.class);

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private ConfigDao configDao;

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private ReferenceLogDao referenceLogDao;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private CheckEnvConflictService checkEnvConflictService;

    @Resource
    private UserContextService userContext;

    private LoadingCache<String, List<RelativeReference>> relativeReferenceCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build(new CacheLoader<String, List<RelativeReference>>() {
        @Override
        public List<RelativeReference> load(String key) throws Exception {
            return doSearchRelative(key);
        }
    });

    //todo deprecated
    @Override
    public ReferenceInfo getReferenceInfo(String group, String profile) {
        return getReferenceInfo(group, profile, null, null, 0, 0, false);
    }

    @Override
    public ReferenceInfo getReferenceInfo(final String group, final String profile, String groupLike, String dataIdLike, int page, int pageSize, boolean pagination) {
        List<ConfigInfoWithoutPublicStatus> publicConfigs;
        int totalCount;
        if (pagination) {
            publicConfigs = configDao.findPublicConfigsInProfileAndResources(Environment.fromProfile(profile),
                    userContext.getAccountGroups(), groupLike, dataIdLike, page, pageSize);
            totalCount = configDao.countPublicConfigsInProfileAndResources(Environment.fromProfile(profile),
                    userContext.getAccountGroups(), groupLike, dataIdLike, page, pageSize);
        } else {
            // todo 旧界面下线后废弃
            publicConfigs = configDao.findPublicedConfigsInProfileAndResources(Environment.fromProfile(profile));
            totalCount = publicConfigs.size();
        }

        final Set<ConfigMeta> referenceMetas = referenceDao.findEverReferences(group, profile).stream().map(input -> new ConfigMeta(input.getRefGroup(), input.getRefDataId(), input.getRefProfile())).collect(Collectors.toSet());

        // todo 旧界面下线后废弃
        return new ReferenceInfo(permissionService.hasPermission(group, profile, PermissionType.EDIT), totalCount,
                publicConfigs.stream().filter(input -> !input.getGroup().equals(group) || !input.getProfile().equals(profile)).filter(input -> userContext.getAccountGroups().contains(input.getGroup())).filter(input -> !referenceMetas.contains(new ConfigMeta(input.getGroup(), input.getDataId(), input
                        .getProfile()))).collect(Collectors.toList()));
    }

    @Override
    public int addReference(Reference reference) throws ModifiedException, ConfigExistException {
        ConfigMeta source = new ConfigMeta(reference.getGroup(), reference.getAlias(), reference.getProfile());
        Optional<Conflict> conflict = checkEnvConflictService.getConflict(source);
        if (conflict.isPresent()) {
            throw new ConfigExistException(conflict.get());
        }

        try {
            int result = referenceDao.create(reference);
            if (result != 0) {
                referenceLogDao.create(new ReferenceLog(reference, RefChangeType.ADD));
            }

            return result;
        } catch (DuplicateKeyException e) {
            throw new ModifiedException();
        }
    }

    @Override
    public List<ReferenceLog> findConfigsRefer(ConfigMeta meta, RefType refType) {
        return referenceLogDao.find(meta, refType);
    }

    @Override
    public ConfigMeta findReference(ConfigMeta meta) {
        return referenceDao.findReference(meta);
    }

    @Override
    public List<Reference> findEverReferences(String group, String profile) {
        return referenceDao.findEverReferences(group, profile);
    }

    @Override
    public int beReferenceCount(ConfigMeta refMeta) {
        return referenceDao.referenceCount(refMeta);
    }

    @Override
    public int beInheritCount(ConfigMeta refMeta) {
        return referenceDao.referenceCount(refMeta, RefType.INHERIT.value());
    }

    @Override
    public List<RelativeReference> searchRelative(String group) {
        return relativeReferenceCache.getUnchecked(group);
    }

    private List<RelativeReference> doSearchRelative(String group) {
        logger.info("start do search relative reference, group [{}]", group);
        Monitor.referenceRelativeDoSearchCounter.inc();
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<ConfigMeta> references = referenceDao.findReferences(group);
            Set<String> refGroups = Sets.newHashSetWithExpectedSize(references.size() + 1);
            for (ConfigMeta reference : references) {
                refGroups.add(reference.getGroup());
            }
            refGroups.add(group);
            List<Reference> allReferences = referenceDao.findByReferences(refGroups);
            Map<ConfigMeta, List<ConfigMeta>> refInfos = Maps.newHashMap();
            for (Reference reference : allReferences) {
                ConfigMeta refMeta = new ConfigMeta(reference.getRefGroup(), reference.getRefDataId(), reference.getRefProfile());
                ConfigMeta meta = new ConfigMeta(reference.getGroup(), reference.getAlias(), reference.getProfile());
                List<ConfigMeta> metas = refInfos.computeIfAbsent(refMeta, k -> Lists.newArrayList());
                metas.add(meta);
            }

            List<RelativeReference> result = Lists.newArrayList();
            for (Map.Entry<ConfigMeta, List<ConfigMeta>> refInfo : refInfos.entrySet()) {
                if (isGroupRefInfo(group, refInfo)) {
                    result.add(new RelativeReference(refInfo.getKey(), refInfo.getValue()));
                }
            }
            return result;
        } catch (RuntimeException e) {
            logger.error("error do search relative reference, group [{}]", group, e);
            Monitor.referenceRelativeDoSearchErrorCounter.inc();
            throw e;
        } finally {
            Monitor.referenceRelativeDoSearchTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private boolean isGroupRefInfo(String group, Map.Entry<ConfigMeta, List<ConfigMeta>> refInfo) {
        ConfigMeta refMeta = refInfo.getKey();
        if (refMeta.getGroup().equals(group)) {
            return true;
        }
        for (ConfigMeta meta : refInfo.getValue()) {
            if (meta.getGroup().equals(group)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public int removeReference(Reference reference) {
        int result = referenceDao.delete(reference);
        if (result != 0) {
            referenceLogDao.create(new ReferenceLog(reference, RefChangeType.REMOVE));
            configUsedLogDao.delete(new ConfigMeta(reference.getGroup(),reference.getAlias(),reference.getProfile()));
        }

        return result;
    }
}
