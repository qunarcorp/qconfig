package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.cloud.vo.EditPushHostVO;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.web.bean.PushStatus;
import qunar.tc.qconfig.admin.web.bean.PushType;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;

public interface PushHistoryService {

    List<ConfigMeta> getUnRollbackPushList(String group, String profile);

    boolean checkIsPushRollback(ConfigMeta meta);

    /**
     * 如果是发布，传入机器可以为空
     *
     * @param candidateSnapshot 发布内容
     * @param pushMachine       机器列表
     * @param type              发布类型
     */
    void addOneHistory(CandidateSnapshot candidateSnapshot, List<PushItemWithHostName> pushMachine, PushType type);

    void addGreyReleaseHistory(ConfigMeta meta, long version, List<Host> machineList);

    Map<String, Integer> getFileEditPushMachineIPAndPort(ConfigMeta configMeta);

    List<EditPushHostVO> getUnRollBackIPandPort(ConfigMeta configMeta);

    void changeGreyReleaseStatus(ConfigMeta meta, long version, PushStatus status);

    void notifyGreyReleaseFinish(ConfigMeta meta, long version, List<Host> hosts);

}
