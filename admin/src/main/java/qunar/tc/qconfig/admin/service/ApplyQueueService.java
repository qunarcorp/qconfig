package qunar.tc.qconfig.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.model.ApplyResult;
import qunar.tc.qconfig.admin.model.InterData;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.web.bean.ConfigDetail;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * User: zhaohuiyu
 * Date: 5/14/14
 * Time: 6:18 PM
 */
public interface ApplyQueueService {

    /**
     * 申请
     */
    ApplyResult apply(CandidateDTO dto);

    /**
     * 强制提交，提交时不检查文件是否存在或已被删除，因为这个方法仅在创建一个与已删除的文件同名的文件时调用，
     * 直接把已删除的文件状态改为“待审核”，并更新其版本号和snapshot，流程与更新一个已发布的版本一致
     */
    void forceApply(CandidateDTO dto);

    boolean approveAndPublish(ConfigMeta configMeta);

    InterData oneButtonPublish(CandidateDTO dto, boolean isForceApply);

    List<InterData> oneButtonBatchPublish(List<CandidateDTO> candidateDTOList);

    List<InterData> internalBatchSave(List<ConfigDetail> configDetails, boolean isPublic, boolean withPriority) throws FileNotFoundException, JsonProcessingException;

    /**
     * 使文件变为公开状态
     * 返回true代表从非公开状态转到公开状态，false代表没有改变状态
     */
    boolean makePublic(ConfigMeta configMeta);

    boolean makePublic(List<CandidateDTO> candidateDTOList);

    /**
     * 拒绝
     */
    void reject(CandidateDTO dto);

    /**
     * 审核
     */
    void approve(CandidateDTO dto);

    /**
     * 取消审核
     */
    void cancel(CandidateDTO dto);

    /**
     * 发布
     */
    CandidateSnapshot publish(CandidateDTO dto);

    void push(CandidateDTO dto, List<PushItemWithHostName> destinations);

    void pushEditing(CandidateDTO dto, List<PushItemWithHostName> destinations);

    void delete(CandidateDTO candidateDTO);

    void greyBatchPublish(CandidateDTO dto);

    boolean makeInherit(ConfigMeta configMeta);

    boolean makeRest(ConfigMeta configMeta);
}
