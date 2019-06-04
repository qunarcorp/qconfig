package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.SnapshotDao;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.DiffService;
import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static qunar.tc.qconfig.common.util.Constants.NO_FILE_VERSION;

@Service
public class DiffServiceImpl implements DiffService {

    private final Logger logger = LoggerFactory.getLogger(DiffServiceImpl.class);

    @Resource
    private ConfigService configService;

    @Resource
    private SnapshotDao snapshotDao;

    @Resource
    private Differ differ;

    public Differ.MixedDiffResult<String, String> getHtmlMixedDiff(String oldData, String newData, String dataId) {
        DiffResult<String> htmlDiff = differ.diffToHtml(oldData, newData, dataId);
        DiffResult<String> uniDiff = differ.uniDiff(oldData, newData, dataId);
        return new Differ.MixedDiffResult<>(htmlDiff, uniDiff);
    }

    @Override
    public Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>> getHtmlMixedDiffToLastPublish(ConfigMeta meta, String data) {
        VersionData<String> oldVersionData = configService.getCurrentPublishedData(meta);
        DiffResult<String> htmlDiff = differ.diffToHtml(oldVersionData.getData(), data, meta.getDataId());
        DiffResult<String> uniDiff = differ.uniDiff(oldVersionData.getData(), data, meta.getDataId());
        return Maps.immutableEntry(new VersionData<>(oldVersionData.getVersion(), meta), new Differ.MixedDiffResult<>(htmlDiff, uniDiff));
    }

    @Override
    public List<Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>>> getHtmlMixedDiffToRelativeProfile(ConfigMeta meta, String data) {
        List<VersionData<ConfigMeta>> mappedConfigs = configService.getMappedConfigs(meta);
        List<Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>>> results = Lists.newArrayList();
        for (VersionData<ConfigMeta> configId : mappedConfigs) {
            if (configId.getVersion() == NO_FILE_VERSION) {
                results.add(Maps.immutableEntry(configId, null));
                continue;
            }
            String betaData = snapshotDao.find(configId).getData();
            betaData = configService.templateDataLongToStr(configId.getData().getGroup(), configId.getData().getDataId(), betaData);
            DiffResult<String> htmlDiff = differ.diffToHtml(betaData, data, meta.getDataId());
            DiffResult<String> uniDiff = differ.uniDiff(betaData, data, meta.getDataId());
            results.add(Maps.immutableEntry(configId, new Differ.MixedDiffResult<>(htmlDiff, uniDiff)));
        }
        return results;
    }

}
