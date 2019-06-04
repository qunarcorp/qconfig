package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author keli.wang
 * @since 2017/11/14
 */
public interface ConfigVersionsService {
    List<CandidateSnapshot> recentPublishedSnapshots(final ConfigMeta configMeta);
}
