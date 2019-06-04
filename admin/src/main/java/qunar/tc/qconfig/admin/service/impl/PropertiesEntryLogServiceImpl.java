package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.dao.PropertiesEntryLogDao;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.PropertiesEntryDiff;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.PropertiesEntryLogService;
import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PropertiesEntryLogServiceImpl implements PropertiesEntryLogService {

    private final Logger logger = LoggerFactory.getLogger(PropertiesEntryLogServiceImpl.class);

    private final static long VERSION_NOT_EXIST = 0;
    private final static int MAX_KEY_LENGTH = 100;
    private final static int MAX_COMMENT_LENGTH = 255;

    @Resource
    private ConfigService configService;

    @Resource
    private Differ differ;

    @Resource
    private PropertiesEntryLogDao propertiesEntryLogDao;

    @Resource
    private ConfigEditorSettingsService configEditorSettingsService;

    @Override
    public PaginationResult<PropertiesEntryDiff> listEntries(Set<String> groups, String profile, String dataId, String key,
                                                             String profileLike, String dataIdLike, String keyLike, int page, int pageSize) {
        List<Long> ids = propertiesEntryLogDao.selectLatestIdsOfDistinctKeys(groups, profile, dataId, key, profileLike, dataIdLike, keyLike, page, pageSize);
        if (CollectionUtils.isEmpty(ids)) {
            return new PaginationResult<>(ImmutableList.of(), page, pageSize, 0);
        }
        List<PropertiesEntryDiff> entries = propertiesEntryLogDao.selectByIds(ids);
        int totalCount = propertiesEntryLogDao.countDistinctKeys(groups, profile, dataId, key, profileLike, dataIdLike, keyLike);
        return new PaginationResult<>(entries, page, pageSize, totalCount);
    }

    @Override
    public PaginationResult<PropertiesEntryDiff> listEntryLogs(ConfigMeta meta, String key, int page, int pageSize) {
        List<PropertiesEntryDiff> propertiesEntryDiffs = propertiesEntryLogDao.selectKey(meta, key, page, pageSize);
        int totalCount = propertiesEntryLogDao.countKey(meta, key);
        return new PaginationResult<>(propertiesEntryDiffs, page, pageSize, totalCount);
    }

    @Override
    public void deleteEntryLog(ConfigMeta meta) {
        logger.info("delete properties entry log, configMeta:{}", meta);
        try {
            propertiesEntryLogDao.delete(meta);
        } catch (Exception e) {
            logger.error("delete properties entry log, configMeta:{}", meta, e);
            throw new RuntimeException("delete properties entry log failed");
        }
    }

    @Override
    public void saveEntryLog(CandidateSnapshot currentSnapshot) {
        if (!needLog(currentSnapshot)) {
            return;
        }
        ConfigMeta meta = new ConfigMeta(currentSnapshot.getGroup(), currentSnapshot.getDataId(), currentSnapshot.getProfile());
        CandidateSnapshot lastPublish = configService.findLastPublish(meta, currentSnapshot.getEditVersion());
        String oldData = "";
        String currentData = currentSnapshot.getData();
        long lastVersion = VERSION_NOT_EXIST;
        if (lastPublish != null) {
            oldData = lastPublish.getData();
            lastVersion = lastPublish.getEditVersion();
        }
        DiffResult<List<Differ.PropertyDiffDto>> diffResult = differ.getPropertiesDiff(oldData, currentData);
        if (!diffResult.getDiffCount().hasDiff()) {
            return;
        }
        Map<String, String> keyCommentMap = differ.getKeyCommentMapping(currentData);
        List<PropertiesEntryDiff> changed = Lists.newArrayList();
        // 旧有文件首次记录entry log时，没变更的key标记为init状态, 也可供查询。
        if (lastPublish != null && !propertiesEntryLogDao.isMetaLogExist(meta)) {
            MapDifference<String, String> mapDiffs = differ.getMapDifference(oldData, currentData);
            for (Map.Entry<String, String> commonEntry : mapDiffs.entriesInCommon().entrySet()) {
                VersionData<String> current = new VersionData<>(currentSnapshot.getEditVersion(), commonEntry.getValue());
                VersionData<String> last = new VersionData<>(lastVersion, commonEntry.getValue());
                String key = commonEntry.getKey();
                if (key == null || key.length() > MAX_KEY_LENGTH) {
                    continue;
                }
                changed.add(new PropertiesEntryDiff(meta, key, current, last, getKeyComment(keyCommentMap, key),
                        PropertiesEntryDiff.EntryDiffType.INIT, currentSnapshot.getOperator()));
            }
        }
        for (Differ.PropertyDiffDto diffDto : diffResult.getResult()) {
            String key = diffDto.getKey();
            // 超长的key不记录
            if (key == null || key.length() > MAX_KEY_LENGTH) {
                continue;
            }
            VersionData<String> current = new VersionData<>(currentSnapshot.getEditVersion(), Strings.nullToEmpty(diffDto.getrValue()));
            VersionData<String> last = new VersionData<>(lastVersion, Strings.nullToEmpty(diffDto.getlValue()));
            changed.add(new PropertiesEntryDiff(meta, key, current, last, getKeyComment(keyCommentMap, key),
                    trans(diffDto.getType()), currentSnapshot.getOperator()));
        }
        logger.info("log properties changed entries, configMeta:{}, version:{}, changedNum:{}", meta, currentSnapshot.getEditVersion(), changed.size());
        try {
            propertiesEntryLogDao.batchInsert(changed);
        } catch (Exception e) {
            logger.error("log properties changed entries error, candidateSnapshot:[{}]", currentSnapshot, e);
            throw new RuntimeException("batch insert properties changed entries failed");
        }
    }

    private String getKeyComment(Map<String, String> keyCommentMap, String key) {
        String comment = keyCommentMap.get(key);
        if (comment == null) {
            return "";
        }
        if (comment.length() > MAX_COMMENT_LENGTH) {
            return comment.substring(0, MAX_COMMENT_LENGTH);
        }
        return comment;
    }

    private boolean needLog(CandidateSnapshot snapshot) {
        if (!FileChecker.isPropertiesFile(snapshot.getDataId())) {
            return false;
        }
        return configEditorSettingsService.settingsOf(snapshot.getGroup(), snapshot.getDataId()).isUseAdvancedEditor();
    }

    private PropertiesEntryDiff.EntryDiffType trans(Differ.DiffType diffType) {
        switch (diffType) {
            case ADDED:
                return PropertiesEntryDiff.EntryDiffType.ADDED;
            case DELETED:
                return PropertiesEntryDiff.EntryDiffType.DELETED;
            case MODIFIED:
                return PropertiesEntryDiff.EntryDiffType.MODIFIED;
            default:
                return PropertiesEntryDiff.EntryDiffType.INIT;
        }
    }
}
