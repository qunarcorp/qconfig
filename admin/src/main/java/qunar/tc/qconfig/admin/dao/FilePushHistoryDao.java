package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.FilePushHistory;
import qunar.tc.qconfig.admin.web.bean.PushStatus;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * Created by pingyang.yang on 2018/10/23
 */
public interface FilePushHistoryDao {

    int insert(FilePushHistory filePushHistory);

    void batchInsert(List<FilePushHistory> filePushHistories);

    int updateStatus(String group, String profile, String dataId, int version, PushStatus status);

    int updateStatus(String group, String profile, String dataId, int version, String ip, PushStatus status);

    List<FilePushHistory> getEditPushHistory(String group, String profile, String dataId, int publishVersion);

    int completeDelete(ConfigMeta meta);
}
