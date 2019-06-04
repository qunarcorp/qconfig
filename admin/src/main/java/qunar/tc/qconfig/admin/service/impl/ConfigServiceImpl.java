package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.sksamuel.diffpatch.DiffMatchPatch;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaVo;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dao.SnapshotDao;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.ConfigInfo;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.DiffCount;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.FileDiffInfo;
import qunar.tc.qconfig.admin.model.KeyValuePair;
import qunar.tc.qconfig.admin.model.ProfileInfo;
import qunar.tc.qconfig.admin.model.PublishedConfigInfo;
import qunar.tc.qconfig.admin.model.UnpublisedConfigInfo;
import qunar.tc.qconfig.admin.service.CandidateLevelService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.ConsumerService;
import qunar.tc.qconfig.admin.service.DiffService;
import qunar.tc.qconfig.admin.service.FileDescriptionService;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.PushHistoryService;
import qunar.tc.qconfig.admin.service.UserBehaviorService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.template.TemplateUtils;
import qunar.tc.qconfig.admin.support.DiffUtil;
import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.admin.support.JsonUtil;
import qunar.tc.qconfig.admin.web.security.PermissionType;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PublicStatus;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static qunar.tc.qconfig.common.util.Constants.NO_FILE_VERSION;

/**
 * User: zhaohuiyu Date: 5/14/14 Time: 5:52 PM
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private static Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
    @Resource
    private Differ differ;

    @Resource
    private DiffService diffService;

    @Resource
    private ConfigDao configDao;

    @Resource
    private SnapshotDao snapshotDao;

    @Resource(name="referenceDao")
    private ReferenceDao referenceDao;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private UserContextService userContext;

    @Resource
    private CandidateDao candidateDao;

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private FileDescriptionService fileDescriptionService;

    @Resource
    private FileTemplateService fileTemplateService;

    @Resource
    private CandidateLevelService candidateLevelService;

    @Resource
    private UserBehaviorService userBehaviorService;

    @Override
    public ConfigInfoWithoutPublicStatus findConfigWithoutPublicStatus(ConfigMeta configMeta) {
        return configDao.findCurrentConfigInfo(configMeta);
    }

    @Override
    public CandidateSnapshot findLastCandidateSnapshot(ConfigMeta configMeta) {
        checkArgument(configMeta != null, "configMeta 不能为空");
        return candidateSnapshotDao.findLatestCandidateSnapshot(configMeta);
    }

    @Override
    public List<CandidateSnapshot> findPublishedCandidateInAppLast30(List<String> groups) {
        long currentTime = System.currentTimeMillis();
        long beginTime = currentTime - 60L * 60L * 24L * 30L * 1000L;
        return candidateSnapshotDao.findPublishedCandidateSnapshotsWithApps(groups, new Date(beginTime));
    }

    @Override
    public ConfigInfoWithoutPublicStatus findPublishedConfigWithoutPublicStatus(ConfigMeta configMeta) {
        ConfigInfoWithoutPublicStatus config = configDao.findCurrentConfigInfo(configMeta);
        if (config == null || !config.isInuse()) {
            return null;
        }
        return config;
    }

    @Override
    public long currentVersionIncludeDeleted(ConfigMeta configMeta) {
        ConfigInfoWithoutPublicStatus current = configDao.findCurrentConfigInfo(configMeta);
        return current == null ? 0 : current.getVersion();
    }

    @Override
    @Transactional
    public CandidateSnapshot publish(CandidateSnapshot snapshot) throws ModifiedException {
        logger.info("publish config, {}", snapshot);
        checkArgument(snapshot.getStatus() == StatusType.PUBLISH, "status should be publish: %s", snapshot.getStatus());

        createOrUpdateConfig(snapshot);
        saveSnapshot(snapshot);

        return snapshot;
    }

    @Override
    @Transactional
    public void batchPublish(final List<CandidateSnapshot> snapshotList) {
        tryBatchCreateOrUpdateConfig(snapshotList);
        snapshotDao.batchSave(snapshotList);
    }

    private void tryBatchCreateOrUpdateConfig(final List<CandidateSnapshot> snapshotList) {
        List<CandidateSnapshot> saveList = Lists.newLinkedList();
        List<CandidateSnapshot> updateList = Lists.newLinkedList();

        for (CandidateSnapshot snapshot : snapshotList) {
            if (snapshot.getBasedVersion() == 0) {
                saveList.add(snapshot);
            } else {
                updateList.add(snapshot);
            }
        }
        try {
            configDao.batchUpdate(updateList);
            configDao.batchSave(saveList);
        } catch (DuplicateKeyException e) {
            throw new ModifiedException();
        }
    }

    private void createOrUpdateConfig(final CandidateSnapshot snapshot) throws ModifiedException {
        final ConfigMeta configMeta = new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile());
        final ConfigInfoWithoutPublicStatus currentConfig = configDao.findCurrentConfigInfo(configMeta);
        final long version = currentConfig == null ? 0 : currentConfig.getVersion();

        if (version != snapshot.getBasedVersion()) {
            throw new ModifiedException();
        } else if (version == 0) {
            try {
                configDao.create(VersionData.of(snapshot.getEditVersion(), configMeta));
            } catch (DuplicateKeyException e) {
                throw new ModifiedException();
            }
        } else {
            int update = configDao.update(VersionData.of(snapshot.getEditVersion(), configMeta),
                    snapshot.getBasedVersion(),
                    PublicStatus.INUSE);
            if (update == 0) {
                throw new ModifiedException();
            }
        }
    }

    @Override
    public boolean saveSnapshot(ConfigMeta meta, long version) {
        CandidateSnapshot snapshot = getCandidateDetails(meta.getGroup(), meta.getDataId(), meta.getProfile(), version);
        if (snapshot != null) {
            // todo: check exist before save
            saveSnapshot(snapshot);
            return true;
        }
        return false;
    }

    private void saveSnapshot(CandidateSnapshot snapshot) {
        String checksum = ChecksumAlgorithm.getChecksum(snapshot.getData());
        ChecksumData<String> checksumData = ChecksumData.of(checksum, snapshot.getData());
        snapshotDao.save(VersionData.of(snapshot.getEditVersion(),
                new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile())),
                checksumData,
                snapshot.getBasedVersion());
    }

    @Override
    public ProfileInfo getProfileInfo(String group, String profile, String keyword) {
        return getProfileInfo(group, profile, null, keyword);
    }

    @Override
    // 若dataId不为空则精确查文件名，否则按keyword模糊查文件名，若keyword也为空则列出环境下所有文件
    public ProfileInfo getProfileInfo(String group, String profile, String dataId, String keyword) {
        Map<String, String> descriptions = fileDescriptionService.getDescriptions(group);
        Map<String, PublishedConfigInfo> publishedMap = findPublished(group, profile, dataId, keyword, descriptions);//已发布配置集合
        Map<String, UnpublisedConfigInfo> unPublishedMap = findUnpublished(group, profile, dataId, keyword, descriptions);
        for (Map.Entry<String, UnpublisedConfigInfo> unpublished : unPublishedMap.entrySet()) {
            PublishedConfigInfo publishedConfigInfo = publishedMap.get(unpublished.getKey());
            if (publishedConfigInfo != null) {
                publishedConfigInfo.setHasBeenModified(true);
                unpublished.getValue().setPublished(true);
            }
        }
        List<PublishedConfigInfo> referenced = findReferenced(group, profile, dataId, keyword);
        return generateProfileInfo(group, profile, Lists.newArrayList(publishedMap.values()), unPublishedMap.values(), referenced);
    }

    private KeyValuePair<Integer, Integer> computeOffsetAndLimit(int offset, int limit, int total){
        //1.第一条数据源不是本数据源，limit为正时需要加载本数据源，大小为剩下的数据源大小，为负时不需要加载本数据源。
        //2.第一条数据源是本数据源。减去以前加载数据源即可，limit为默认的。
        if (total > offset){
            return new KeyValuePair<>(0, limit - (total - offset));
        } else {
            return new KeyValuePair<>(offset - total, limit);
        }
    }

    public ProfileInfo getProfileInfoPage(String group, String profile, String dataId, String keyword, int start, int pageSize) {
        int unpublishCount = configDao.countUnpublicFile(group,profile, keyword);
        int publishCount = configDao.countPublicFile(group ,profile, keyword);
        int referenceCount = configDao.countReferenceFile(group, profile, keyword);
        int count = unpublishCount + publishCount + referenceCount;
        Map<String, String> descriptions = fileDescriptionService.getDescriptions(group);

        Map<String, UnpublisedConfigInfo> unPublishedMap = findUnpublishedPage(group, profile, dataId, keyword, descriptions, start, pageSize);
        Map<String, PublishedConfigInfo> publishedMap = Maps.newHashMap();
        List<PublishedConfigInfo> referenced = Lists.newArrayList();

        KeyValuePair<Integer, Integer> offsetAndLimit = computeOffsetAndLimit(start, pageSize, unpublishCount);
        if (offsetAndLimit.getValue() > 0) {
            publishedMap = findPublishedPage(group, profile, dataId, keyword, descriptions, offsetAndLimit.getKey(), offsetAndLimit.getValue());
            offsetAndLimit = computeOffsetAndLimit(start, pageSize, unpublishCount + publishCount);
            if (offsetAndLimit.getValue() > 0) {
                referenced = findReferencedPage(group, profile, dataId, keyword, offsetAndLimit.getKey(), offsetAndLimit.getValue());
            }
        }
        return generateProfileInfoPage(group, profile, Lists.newArrayList(publishedMap.values()), unPublishedMap.values(), referenced, count);
    }

    @Override
    public List<PublishedConfigInfo> getPublishedConfig(String group, String profile) {
        return configDao.findPublished(group, profile);
    }

    @Override
    public PublishedConfigInfo getConfigInfo(String group, String profile, String dataId) {
        boolean isReference = false;
        List<PublishedConfigInfo> published = configDao.findPublished(group, profile, dataId);
        if (CollectionUtils.isEmpty(published)) {
            published = referenceDao.findReferenceDetailByMeta(group, profile, dataId);
            isReference = true;
            if (CollectionUtils.isEmpty(published)) {
                return null;
            }
        }
        Preconditions.checkState(published.size() == 1);
        PublishedConfigInfo configInfo = published.get(0);
        if(configInfo != null && configInfo.getConfigMeta() != null) {
            String description = isReference ? fileDescriptionService.getDescription(configInfo.getRefConfigMeta().getGroup(),
                    configInfo.getRefConfigMeta().getDataId()) : fileDescriptionService.getDescription(group, dataId);
            configInfo.setDescription(description);
            if (!isReference) {
                configInfo.setOperator(candidateSnapshotDao.findOperator(group, configInfo.getConfigMeta().getDataId(), profile, configInfo.getVersion()));
            }
        }
        List<PublishedConfigInfo> filteredConfigInfos = (List<PublishedConfigInfo>) filterNoAccessibleAndSetFilePermission(published);
        return !CollectionUtils.isEmpty(filteredConfigInfos) ? filteredConfigInfos.get(0) : null;
    }

    @Override
    public List<PublishedConfigInfo> getConfigInfo(List<ConfigMeta> metas) {
        List<PublishedConfigInfo> published = configDao.findPublished(metas);
        if (CollectionUtils.isEmpty(published)) {
            return ImmutableList.of();
        }
        Multimap<String, String> multimap = HashMultimap.create();
        for (ConfigMeta meta : metas) {
            multimap.put(meta.getGroup(), meta.getDataId());
        }
        Table<String, String, String> descriptions = fileDescriptionService.getDescriptions(multimap);
        for (PublishedConfigInfo info : published) {
            ConfigMeta meta = info.getConfigMeta();
            info.setOperator(candidateSnapshotDao.findOperator(meta.getGroup(), meta.getDataId(), meta.getProfile(), info.getVersion()));
            info.setDescription(descriptions.get(meta.getGroup(), meta.getDataId()));
        }
        return published;
    }

    // 已发布配置
    private Map<String, PublishedConfigInfo> findPublished(String group, String profile, String dataId, String keyword, Map<String, String> descriptions) {
        Map<String, PublishedConfigInfo> publishedMap = Maps.newHashMap();
        List<PublishedConfigInfo> publishedConfigInfos;
        if (StringUtils.isNotBlank(dataId)) {
            publishedConfigInfos = configDao.findPublished(group, profile, dataId);
        } else if (StringUtils.isNotBlank(keyword)) {
            publishedConfigInfos = configDao.findPublishedWithKeyword(group, profile, keyword);
        } else {
            publishedConfigInfos = configDao.findPublished(group, profile);
        }
        return getStringPublishedConfigInfoMap(group, profile, descriptions, publishedMap, publishedConfigInfos);
    }

    private Map<String, PublishedConfigInfo> getStringPublishedConfigInfoMap(String group, String profile, Map<String, String> descriptions, Map<String, PublishedConfigInfo> publishedMap, List<PublishedConfigInfo> publishedConfigInfos) {
        for (PublishedConfigInfo configInfo : publishedConfigInfos) {
            if(configInfo != null && configInfo.getConfigMeta() != null) {
                String itemDataId = configInfo.getConfigMeta().getDataId();
                configInfo.setOperator(candidateSnapshotDao.findOperator(group, itemDataId, profile, configInfo.getVersion()));
                configInfo.setDescription(descriptions.get(itemDataId));
                publishedMap.put(itemDataId, configInfo);
            }
        }
        return publishedMap;
    }

    private Map<String, PublishedConfigInfo> findPublishedPage(String group, String profile, String dataId, String keyword, Map<String, String> descriptions, int start, int pageSize) {
        Map<String, PublishedConfigInfo> publishedMap = Maps.newHashMap();
        List<PublishedConfigInfo> publishedConfigInfos;
        if (StringUtils.isNotBlank(dataId)) {
            publishedConfigInfos = configDao.findPublishedPage(group, profile, dataId, start, pageSize);
        } else if (StringUtils.isNotBlank(keyword)) {
            publishedConfigInfos = configDao.findPublishedWithKeywordPage(group, profile, keyword, start, pageSize);
        } else {
            publishedConfigInfos = configDao.findPublishedPage(group, profile, start, pageSize);
        }
        return getStringPublishedConfigInfoMap(group, profile, descriptions, publishedMap, publishedConfigInfos);
    }

    // 未发布配置
    private Map<String, UnpublisedConfigInfo> findUnpublishedPage(String group, String profile, String dataId, String keyword, Map<String, String> descriptions, int start, int pageSize) {
        Map<String, UnpublisedConfigInfo> unPublishedMap = Maps.newHashMap();
        List<UnpublisedConfigInfo> unpublishedCandidates;
        if (StringUtils.isNotBlank(dataId)) {
            unpublishedCandidates = candidateDao.findUnpublishedWithDataIdPage(group, profile, dataId, Arrays.asList(StatusType.PENDING, StatusType.PASSED, StatusType.REJECT, StatusType.CANCEL), start, pageSize);
        } else if (StringUtils.isNotBlank(keyword)) {
            unpublishedCandidates = candidateDao.findUnpublishedWithKeywordPage(group, profile, keyword, Arrays.asList(StatusType.PENDING, StatusType.PASSED, StatusType.REJECT, StatusType.CANCEL), start, pageSize);
        } else {
            unpublishedCandidates = candidateDao.findUnpublishedPage(group, profile, Arrays.asList(StatusType.PENDING, StatusType.PASSED, StatusType.REJECT, StatusType.CANCEL), start, pageSize);
        }
        return getStringUnpublisedConfigInfoMap(group, profile, descriptions, unPublishedMap, unpublishedCandidates);
    }

    private Map<String, UnpublisedConfigInfo> getStringUnpublisedConfigInfoMap(String group, String profile, Map<String, String> descriptions, Map<String, UnpublisedConfigInfo> unPublishedMap, List<UnpublisedConfigInfo> unpublishedCandidates) {
        for (UnpublisedConfigInfo unPublished : unpublishedCandidates) {
            String itemDataId = unPublished.getConfigMeta().getDataId();
            unPublished.setOperator(candidateSnapshotDao.findOperator(group, itemDataId, profile, unPublished.getEditVersion()));
            unPublished.setDescription(descriptions.get(itemDataId));
            unPublishedMap.put(itemDataId, unPublished);
        }
        return unPublishedMap;
    }


    // 未发布配置
    private Map<String, UnpublisedConfigInfo> findUnpublished(String group, String profile, String dataId, String keyword, Map<String, String> descriptions) {
        Map<String, UnpublisedConfigInfo> unPublishedMap = Maps.newHashMap();
        List<UnpublisedConfigInfo> unpublishedCandidates;
        if (StringUtils.isNotBlank(dataId)) {
            unpublishedCandidates = candidateDao.findUnpublishedWithDataId(group, profile, dataId, Arrays.asList(StatusType.PENDING, StatusType.PASSED, StatusType.REJECT, StatusType.CANCEL));
        } else if (StringUtils.isNotBlank(keyword)) {
            unpublishedCandidates = candidateDao.findUnpublishedWithKeyword(group, profile, keyword, Arrays.asList(StatusType.PENDING, StatusType.PASSED, StatusType.REJECT, StatusType.CANCEL));
        } else {
            unpublishedCandidates = candidateDao.findUnpublished(group, profile, Arrays.asList(StatusType.PENDING, StatusType.PASSED, StatusType.REJECT, StatusType.CANCEL));
        }
        return getStringUnpublisedConfigInfoMap(group, profile, descriptions, unPublishedMap, unpublishedCandidates);
    }

    private List<PublishedConfigInfo> findReferenced(String group, String profile, String dataId, String keyword) {
        List<PublishedConfigInfo> reference;
        if (StringUtils.isNotBlank(dataId)) {
            reference = referenceDao.findReferenceDetailByMeta(group, profile, dataId);
        } else if (StringUtils.isNotBlank(keyword)) {
            reference = referenceDao.findReferenceDetail(group, profile, keyword);
        } else {
            reference = referenceDao.findReferenceDetail(group, profile);
        }
        return getPublishedConfigInfos(reference);
    }

    private List<PublishedConfigInfo> getPublishedConfigInfos(List<PublishedConfigInfo> reference) {
        Multimap<String, String> groupDataIdMappings = HashMultimap.create();
        for (PublishedConfigInfo referenced : reference) {
            groupDataIdMappings.put(referenced.getRefConfigMeta().getGroup(), referenced.getRefConfigMeta().getDataId());
        }
        Table<String, String, String> descriptions = fileDescriptionService.getDescriptions(groupDataIdMappings);
        for (PublishedConfigInfo configInfo : reference) {
            configInfo.setDescription(descriptions.get(configInfo.getRefConfigMeta().getGroup(), configInfo.getRefConfigMeta().getDataId()));
            configInfo.setFavoriteFile(isUserFavoriteFile(configInfo.getConfigMeta()));
        }
        return reference;
    }

    private List<PublishedConfigInfo> findReferencedPage(String group, String profile, String dataId, String keyword, int start, int pageSize) {
        List<PublishedConfigInfo> reference;
        if (StringUtils.isNotBlank(dataId)) {
            reference = referenceDao.findReferenceDetailByMetaPage(group, profile, dataId, start, pageSize);
        } else if (StringUtils.isNotBlank(keyword)) {
            reference = referenceDao.findReferenceDetailPage(group, profile, keyword, start, pageSize);
        } else {
            reference = referenceDao.findReferenceDetailPage(group, profile, start, pageSize);
        }
        return getPublishedConfigInfos(reference);
    }
    private void setFilePermission(ConfigInfo item) {
        item.setEdit(permissionService.hasFilePermission(item.getConfigMeta().getGroup(), item.getConfigMeta().getProfile(), item.getConfigMeta().getDataId(), PermissionType.EDIT));
        item.setApprove(permissionService.hasFilePermission(item.getConfigMeta().getGroup(), item.getConfigMeta().getProfile(), item.getConfigMeta().getDataId(), PermissionType.APPROVE));
        item.setPublish(permissionService.hasFilePermission(item.getConfigMeta().getGroup(), item.getConfigMeta().getProfile(), item.getConfigMeta().getDataId(), PermissionType.PUBLISH));
    }

    private ProfileInfo generateProfileInfo(String group, String profile, Collection<PublishedConfigInfo> published, Collection<UnpublisedConfigInfo> unPublished, List<PublishedConfigInfo> referenceList) {
//        List<UnpublisedConfigInfo> pending = Lists.newArrayList();
//        List<UnpublisedConfigInfo> pass = Lists.newArrayList();
//        List<PublishedConfigInfo> publish = Lists.newArrayList();
//        List<UnpublisedConfigInfo> reject = Lists.newArrayList();
//        List<UnpublisedConfigInfo> cancel = Lists.newArrayList();
//
//        for (UnpublisedConfigInfo configInfo : unPublished) {
//            //TODO isFavorite
//            configInfo.setFavoriteFile(isUserFavoriteFile(configInfo.getConfigMeta()));
//            switch (configInfo.getStatusType()) {
//                case PENDING:
//                    pending.add(configInfo);
//                    break;
//                case PASSED:
//                    pass.add(configInfo);
//                    break;
//                case REJECT:
//                    reject.add(configInfo);
//                    break;
//                case CANCEL:
//                    cancel.add(configInfo);
//                    break;
//                default:
//                    throw new IllegalStateException("unexpected unpublished candidate: " + configInfo);
//            }
//        }
//        publish.addAll(published);
//        for (PublishedConfigInfo configInfo : publish) {
//            configInfo.setFavoriteFile(isUserFavoriteFile(configInfo.getConfigMeta()));
//        }
//        return new ProfileInfo(group, profile, Environment.fromProfile(profile).text(), ProfileUtil.getBuildGroup(profile),
//                permissionService.hasPermission(group, null, PermissionType.EDIT),
//                permissionService.hasPermission(group, profile, PermissionType.EDIT),
//                permissionService.hasPermission(group, profile, PermissionType.APPROVE),
//                permissionService.hasPermission(group, profile, PermissionType.PUBLISH),
//                userContext.isLeaderOf(group),
//                filterNoAccessibleAndSetFilePermission(referenceList),
//                filterNoAccessibleAndSetFilePermission(pending),
//                filterNoAccessibleAndSetFilePermission(pass),
//                filterNoAccessibleAndSetFilePermission(publish),
//                filterNoAccessibleAndSetFilePermission(reject),
//                filterNoAccessibleAndSetFilePermission(cancel));
        return generateProfileInfoPage(group, profile, published, unPublished, referenceList, published.size() + unPublished.size() + referenceList.size());
    }


    private ProfileInfo generateProfileInfoPage(String group, String profile, Collection<PublishedConfigInfo> published, Collection<UnpublisedConfigInfo> unPublished, List<PublishedConfigInfo> referenceList, int count) {
        List<UnpublisedConfigInfo> pending = Lists.newArrayList();
        List<UnpublisedConfigInfo> pass = Lists.newArrayList();
        List<PublishedConfigInfo> publish = Lists.newArrayList();
        List<UnpublisedConfigInfo> reject = Lists.newArrayList();
        List<UnpublisedConfigInfo> cancel = Lists.newArrayList();

        for (UnpublisedConfigInfo configInfo : unPublished) {
            //TODO isFavorite
            configInfo.setFavoriteFile(isUserFavoriteFile(configInfo.getConfigMeta()));
            switch (configInfo.getStatusType()) {
                case PENDING:
                    pending.add(configInfo);
                    break;
                case PASSED:
                    pass.add(configInfo);
                    break;
                case REJECT:
                    reject.add(configInfo);
                    break;
                case CANCEL:
                    cancel.add(configInfo);
                    break;
                default:
                    throw new IllegalStateException("unexpected unpublished candidate: " + configInfo);
            }
        }
        publish.addAll(published);
        for (PublishedConfigInfo configInfo : publish) {
            configInfo.setFavoriteFile(isUserFavoriteFile(configInfo.getConfigMeta()));
        }
        return new ProfileInfo(group, profile, Environment.fromProfile(profile).text(), ProfileUtil.getBuildGroup(profile),
                permissionService.hasPermission(group, null, PermissionType.EDIT),
                permissionService.hasPermission(group, profile, PermissionType.EDIT),
                permissionService.hasPermission(group, profile, PermissionType.APPROVE),
                permissionService.hasPermission(group, profile, PermissionType.PUBLISH),
                userContext.isLeaderOf(group),
                filterNoAccessibleAndSetFilePermission(referenceList),
                filterNoAccessibleAndSetFilePermission(pending),
                filterNoAccessibleAndSetFilePermission(pass),
                filterNoAccessibleAndSetFilePermission(publish),
                filterNoAccessibleAndSetFilePermission(reject),
                filterNoAccessibleAndSetFilePermission(cancel),
                count);
    }

    private List<? extends ConfigInfo> filterNoAccessibleAndSetFilePermission(List<? extends ConfigInfo> items) {
        for (ConfigInfo item : items) {
            setFilePermission(item);
        }

        if (items.isEmpty()) {
            return items;
        }

        if (userContext.hasGroupPermission(items.get(0).getConfigMeta().getGroup())) {
            return items;
        }

        List<ConfigInfo> results = Lists.newArrayList();
        for (ConfigInfo item : items) {
            if (permissionService.hasFilePermission(item.getConfigMeta().getGroup(),
                    item.getConfigMeta().getProfile(), item.getConfigMeta().getDataId(), PermissionType.VIEW)) {
                results.add(item);
            } else if ((item.getPublicType() != null) &&
                    (item.getPublicType().code() & PublicType.PUBLIC_MASK) != 0){
                results.add(item);
            }
        }
        return results;
    }

    @Override
    public CandidateSnapshot getCandidateDetails(String group, String dataId, String profile, long editVersion) {
        return candidateSnapshotDao.find(group, dataId, profile, editVersion);
    }

    @Override
    public Candidate currentEdit(ConfigMeta configMeta) {
        return candidateDao.find(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile());
    }

    @Override
    public CandidateSnapshot currentEditSnapshot(ConfigMeta configMeta) {
        Candidate candidate = candidateDao.find(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile());
        if (candidate == null) {
            return null;
        }
        return candidateSnapshotDao.find(configMeta.getGroup(),
                configMeta.getDataId(),
                configMeta.getProfile(),
                candidate.getEditVersion());
    }

    @Override
    public boolean existWithoutStatus(String group, String profile, String dataId, StatusType withoutType) {
        return candidateDao.existWithoutStatus(group, profile, dataId, withoutType);
    }

    @Override
    public Optional<String> checkWithoutPublicFile(String profile, String dataId){
        List<String> publicFileGroup = configDao.findPublicGroupByDataId(dataId);
        if (!publicFileGroup.isEmpty()){
            return Optional.of(publicFileGroup.toString());
        }
        return Optional.empty();
    }
    @Override
    public CandidateSnapshot findLastPublish(ConfigMeta configMeta, long version) {
        return candidateSnapshotDao.findLastPublish(configMeta, version);
    }

    @Override
    public Map.Entry<VersionData<ConfigMeta>, DiffResult<String>> getHtmlDiffToLastPublish(ConfigMeta meta, String data) {
        VersionData<String> oldVersionData = getCurrentPublishedData(meta);
        return Maps.immutableEntry(new VersionData<>(oldVersionData.getVersion(), meta), differ.diffToHtml(oldVersionData.getData(), data, meta.getDataId()));
    }

    @Override
    public Map.Entry<VersionData<ConfigMeta>, JsonNode> getJsonDiffToLastPublish(ConfigMeta meta, String data) {
        VersionData<String> oldVersionData = getCurrentPublishedData(meta);
        ObjectNode node = getJsonNode(oldVersionData.getData());
        return Maps.immutableEntry(new VersionData<>(oldVersionData.getVersion(), meta), node);
    }

    private ObjectNode getJsonNode(String oldData) {
        Optional<JsonNode> oldJson = JsonUtil.read(oldData);
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        if (!oldJson.isPresent()) {
            node.put("error", "无法解析相应的json文件");
            return node;
        }
        node.put("data", oldJson.get());
        return node;
    }

    @Override
    public VersionData<String> getCurrentPublishedData(ConfigMeta meta) {
        ConfigInfoWithoutPublicStatus current = findPublishedConfigWithoutPublicStatus(meta);
        String oldData;
        if (current == null) {
            oldData = "";
        } else {
            oldData = candidateSnapshotDao.find(current.getGroup(), current.getDataId(), current.getProfile(), current.getVersion()).getData();
        }
        String data = templateDataLongToStr(meta.getGroup(), meta.getDataId(), oldData);
        return new VersionData<>(current == null ? NO_FILE_VERSION : current.getVersion(), data);
    }

    @Override
    public String templateDataLongToStr(String group, String dataId, String data) {
        if (Strings.isNullOrEmpty(data) || !FileChecker.isTemplateFile(dataId)) {
            return data;
        }

        Optional<String> detail = fileTemplateService.getTemplateDetailByFile(group, dataId);
        if (detail.isPresent()) {
            String templateDetail = detail.get();
            try {
                Optional<String> optional = TemplateUtils.processTimeLongToStr(dataId, data, templateDetail);
                if (optional.isPresent()) {
                    return optional.get();
                }
            } catch (Exception e) {
                logger.warn("can note process time long to str, group is [%s], dataId is [%s]", group, dataId, e);
                return data;
            }
        }
        return data;
    }

    @Override
    public List<Map.Entry<VersionData<ConfigMeta>, DiffResult<String>>> getHtmlProdBetaOrBetaProdDiffs(ConfigMeta meta, String data) {
        List<VersionData<ConfigMeta>> betaConfigIds = getMappedConfigs(meta);
        List<Map.Entry<VersionData<ConfigMeta>, DiffResult<String>>> results = Lists.newArrayList();
        for (VersionData<ConfigMeta> configId : betaConfigIds) {
            if (configId.getVersion() == NO_FILE_VERSION) {
                results.add(Maps.immutableEntry(configId, null));
                continue;
            }
            String betaData = snapshotDao.find(configId).getData();
            betaData = templateDataLongToStr(configId.getData().getGroup(), configId.getData().getDataId(), betaData);
            results.add(Maps.immutableEntry(configId, differ.diffToHtml(betaData, data, meta.getDataId())));
        }
        return results;
    }

    @Override
    public List<Map.Entry<VersionData<ConfigMeta>, JsonNode>> getJsonProdBetaOrBetaProdDiffs(ConfigMeta meta, String data) {
        List<VersionData<ConfigMeta>> betaConfigIds = getMappedConfigs(meta);
        List<Map.Entry<VersionData<ConfigMeta>, JsonNode>> results = Lists.newArrayList();
        for (VersionData<ConfigMeta> configId : betaConfigIds) {
            if (configId.getVersion() == NO_FILE_VERSION) {
                results.add(Maps.immutableEntry(configId, null));
                continue;
            }
            String mappedData = snapshotDao.find(configId).getData();
            results.add(Maps.immutableEntry(configId, getJsonNode(mappedData)));
        }
        return results;
    }

    @Override
    public List<VersionData<ConfigMeta>> getMappedConfigs(ConfigMeta meta) {
        String metaDefaultProfile = Environment.fromProfile(meta.getProfile()).defaultProfile();
        Environment environment = Environment.fromProfile(metaDefaultProfile);
        Optional<Environment> mappedEnvironment = getEnvironmentDiffMapping(environment);
        if (!mappedEnvironment.isPresent()) {
            return ImmutableList.of();
        }
        List<VersionData<ConfigMeta>> mappedConfig = configDao.findInEnvironment(meta.getGroup(), meta.getDataId(), mappedEnvironment.get());
        return !CollectionUtils.isEmpty(mappedConfig) ? mappedConfig : ImmutableList.of(VersionData.of(NO_FILE_VERSION,
                new ConfigMeta(meta.getGroup(), meta.getDataId(), mappedEnvironment.get().profile())));
    }

    private static final Map<Environment, Environment> bidirectionalEnvironmentMapping = ImmutableMap.of(Environment.PROD, Environment.BETA, Environment.BETA, Environment.PROD);

    private Optional<Environment> getEnvironmentDiffMapping(Environment environment) {
        return Optional.ofNullable(bidirectionalEnvironmentMapping.get(environment));
    }

    @Override
    public void delete(CandidateSnapshot snapshot) throws ModifiedException {
        ConfigMeta configMeta = new ConfigMeta(snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile());
        int deleted = configDao.delete(
                VersionData.of(snapshot.getEditVersion(), configMeta),
                snapshot.getBasedVersion());
        if (deleted == 0) {
            throw new ModifiedException();
        }
    }

    @Override
    public List<FileDiffInfo> diffProfile(String group, String lProfile, String rProfile, DiffUtil.DiffType diffType) {
        List<CandidateSnapshot> files = Lists.newArrayList();
        files.addAll(configDao.findCurrentSnapshotsInGroup(group));
        files.addAll(referenceDao.findCurrentSnapshotsInGroup(group));

        Map<String, CandidateSnapshot> lFiles = filesInProfile(files, lProfile);
        Map<String, CandidateSnapshot> rFiles = filesInProfile(files, rProfile);
        return diff(lFiles, rFiles, diffType);
    }

    public List<FileDiffInfo> diffProfileWithUpperLevel(String group, final String lProfile, final String rProfile) {
        List<CandidateSnapshot> files = candidateLevelService.findCandidates(group);
        Map<String, CandidateSnapshot> lFiles = candidateLevelService.findUpperAndSameLevelFiles(files, lProfile);
        Map<String, CandidateSnapshot> rFiles = candidateLevelService.findUpperAndSameLevelFiles(files, rProfile);
        Iterator<Map.Entry<String, CandidateSnapshot>> it = lFiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CandidateSnapshot> lEntry = it.next();
            String dataId = lEntry.getKey();
            if (rFiles.containsKey(dataId) && isSameFile(lEntry.getValue(), rFiles.get(dataId))) {
                it.remove();
                rFiles.remove(dataId);
            }
        }
        return diff(lFiles, rFiles, DiffUtil.DiffType.PLAIN);
    }

    private boolean isSameFile(CandidateSnapshot lhs, CandidateSnapshot rhs) {
        return lhs.getGroup().equals(rhs.getGroup())
                && lhs.getProfile().equals(rhs.getProfile())
                && lhs.getDataId().equals(rhs.getDataId());
    }

    private List<FileDiffInfo> diff(Map<String, CandidateSnapshot> lFiles, Map<String, CandidateSnapshot> rFiles, DiffUtil.DiffType diffType) {
        Set<String> names = Sets.newHashSet();
        names.addAll(lFiles.keySet());
        names.addAll(rFiles.keySet());

        List<FileDiffInfo> diffInfos = Lists.newArrayListWithCapacity(names.size());
        for (String name : names) {
            CandidateSnapshot lFile = lFiles.get(name);
            CandidateSnapshot rFile = rFiles.get(name);
            diffInfos.add(diffFile(name, lFile, rFile, diffType));
        }
        return diffInfos;
    }

    private FileDiffInfo diffFile(String name, CandidateSnapshot lFile, CandidateSnapshot rFile, DiffUtil.DiffType diffType) {
        boolean lExist = lFile != null;
        boolean rExist = rFile != null;
        String lFileData = lExist ? lFile.getData() : "";
        String rFileData = rExist ? rFile.getData() : "";
        FileMetaVo lMeta = lExist ? new FileMetaVo(lFile.getProfile(), lFile.getEditVersion(), lFile.getUpdateTime()) : null;
        FileMetaVo rMeta = rExist ? new FileMetaVo(rFile.getProfile(), rFile.getEditVersion(), rFile.getUpdateTime()) : null;

        if (FileChecker.isJsonFile(name)) {
            Optional<JsonNode> lhs = JsonUtil.read(lFileData);
            if (!lhs.isPresent()) {
                return new FileDiffInfo(name, lExist, rExist, "源json解析出错", null, lMeta, rMeta);
            }
            Optional<JsonNode> rhs = JsonUtil.read(rFileData);
            if (!rhs.isPresent()) {
                return new FileDiffInfo(name, lExist, rExist, "目标json解析出错", null, lMeta, rMeta);
            }
            if (lhs.get().equals(rhs.get())) {
                return new FileDiffInfo(name, lExist, rExist, null, null, lMeta, rMeta);
            }
            Map<String, JsonNode> nodes = Maps.newHashMap();
            nodes.put("source", lhs.get());
            nodes.put("target", rhs.get());
            return new FileDiffInfo(name, lExist, rExist, null, nodes, lMeta, rMeta);
        } else {
            try {
                if (DiffUtil.DiffType.PLAIN == diffType) {
                    // properties特殊格式
                    if (FileChecker.isPropertiesFile(name)) {
                        DiffResult<List<Differ.PropertyDiffDto>> result = differ.getPropertiesDiff(lFileData, rFileData);
                        return new FileDiffInfo(name, lExist, rExist, null, result, lMeta, rMeta);
                    }
                    DiffResult<List<DiffMatchPatch.Diff>> result = differ.diff(lFileData, rFileData, name);
                    DiffCount diffCount = result.getDiffCount();
                    if (diffCount.hasDiff()) {
                        return new FileDiffInfo(name, lExist, rExist, null, DiffUtil.generateDiffVo(name, result), lMeta, rMeta);
                    } else {
                        return new FileDiffInfo(name, lExist, rExist, null, null, lMeta, rMeta);
                    }
                } else {
                    Differ.MixedDiffResult<String, String> htmlMixedDiff = diffService.getHtmlMixedDiff(lFileData, rFileData, name);
                    DiffCount diffCount = htmlMixedDiff.getUniDiff().getDiffCount();
                    if (diffCount.hasDiff()) {
                        return new FileDiffInfo(name, lExist, rExist, null, DiffUtil.wrapDiffVo(name, htmlMixedDiff), lMeta, rMeta);
                    } else {
                        return new FileDiffInfo(name, lExist, rExist, null, null, lMeta, rMeta);
                    }
                }
            } catch (RuntimeException e) {
                return new FileDiffInfo(name, lExist, rExist, e.getMessage(), null, lMeta, rMeta);
            }
        }
    }

    private Map<String, CandidateSnapshot> filesInProfile(List<CandidateSnapshot> filesInGroup, String profile) {
        Map<String, CandidateSnapshot> files = Maps.newHashMap();
        for (CandidateSnapshot snapshot : filesInGroup) {
            if (Objects.equal(profile, snapshot.getProfile())) {
                snapshot.setData(templateDataLongToStr(snapshot.getGroup(), snapshot.getDataId(), snapshot.getData()));
                files.put(snapshot.getDataId(), snapshot);
            }
        }
        return files;
    }

    @Override
    public List<Candidate> findCandidatesWithGroupAndEnvironment(String group, Set<Environment> environments) {
        List<Candidate> candidates = Lists.newArrayList();
        for (Candidate candidate : candidateDao.find(group)) {
            for (Environment environment : environments) {
                if (Environment.fromProfile(candidate.getProfile()).equalsEnv(environment)) {
                    candidates.add(candidate);
                    break;
                }
            }
        }
        return candidates;
    }

    @Override
    public List<Candidate> findCandidates(String group, String profile) {
        return candidateDao.find(group, profile);
    }

    private boolean isUserFavoriteFile(ConfigMeta meta) {
        return userBehaviorService.isFavoriteFile(meta, userContext.getRtxId());
    }
}
