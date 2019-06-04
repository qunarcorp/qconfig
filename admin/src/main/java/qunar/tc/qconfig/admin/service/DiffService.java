package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;
import java.util.Map;

public interface DiffService {

    Differ.MixedDiffResult<String, String> getHtmlMixedDiff(String oldData, String newData, String dataId);

    Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>> getHtmlMixedDiffToLastPublish(ConfigMeta meta, String data);

    List<Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>>> getHtmlMixedDiffToRelativeProfile(ConfigMeta meta, String data);
}
