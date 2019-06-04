package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.exception.ReferencedNowException;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2017 2017/5/12 15:55
 */
public interface AdminOperateService {

    void completeDelete(ConfigMeta meta, DbEnv profile) throws ReferencedNowException;

    void completeDeleteAdminInfo(ConfigMeta meta);

    void completeDeleteLog(ConfigMeta meta);

    void moveFile(ConfigMeta meta, String toProfile);

    void copyGroup(String fromGroup, String toGroup);

    void referenceGroup(String fromGroup, String toGroup);

    int deleteReference(ConfigMeta meta, DbEnv profile);

    int deleteServer(String ip);
}
