package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseState;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

public interface BatchPushTaskDao {

    StatusInfo queryTask(String uuid);

    boolean insertTask(StatusInfo statusInfo);

    boolean updateTaskStatus(StatusInfo statusInfo);

    boolean updateTaskStatus(StatusInfo status, GreyReleaseState notStatus1, GreyReleaseState notStatus2);

    List<StatusInfo> selectUuidIn(List<String> uuids);

    List<StatusInfo> queryHistoryTasks(ConfigMeta meta, long currentPage, long pageSize);
}
