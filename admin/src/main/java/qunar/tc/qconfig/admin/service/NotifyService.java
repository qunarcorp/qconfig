package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import java.util.List;

/**
 * User: zhaohuiyu
 * Date: 5/23/14
 * Time: 4:18 PM
 */
public interface NotifyService {

    void notify(String group, String dataId, String profile);

    void notifyPush(ConfigMeta meta, long version, List<PushItemWithHostName> destinations);

    void notifyPushIp(ConfigMeta meta, long version, List<Host> destinations);

    void notifyReference(Reference reference, RefChangeType changeType);

    void notifyPublic(ConfigMetaWithoutProfile configMeta);

    void notifyAdminDelete(ConfigMeta configMeta);

    void notifyFixedVersion(ConfigMeta meta, String ip, long version);
}
