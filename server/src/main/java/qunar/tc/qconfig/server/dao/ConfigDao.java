package qunar.tc.qconfig.server.dao;

import com.google.common.base.Optional;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.server.domain.ReferenceInfo;
import qunar.tc.qconfig.server.domain.RelationMeta;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 6:20 PM
 */
public interface ConfigDao {

    List<VersionData<ConfigMeta>> loadAll();

    List<VersionData<ConfigMeta>> loadByGroupAndProfile(String group, String profile);

    // TODO deprecated
    ChecksumData<String> load(VersionData<ConfigMeta> configId);

    ChecksumData<String> loadFromCandidateSnapshot(VersionData<ConfigMeta> configId);

    VersionData<ConfigMeta> load(ConfigMeta configMeta);

    Long selectBasedVersion(VersionData<ConfigMeta> configId);

    Optional<ConfigMeta> loadReference(ConfigMeta source, RefType refType);

    List<ReferenceInfo> loadAllReferenceInfo();

    Optional<ReferenceInfo> loadReferenceInfo(RelationMeta relationMeta);

    Optional<ReferenceInfo> loadReferenceInfo(String groupId, String dataId, String profile, String refGroupId, String refDataId, String refProfile, RefType refType);

}
