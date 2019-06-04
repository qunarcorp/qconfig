package qunar.tc.qconfig.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.event.ConfigOperationEvent;
import qunar.tc.qconfig.admin.exception.*;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.InterData;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.web.bean.ConfigDetail;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/26 21:08
 */
public interface EventPostApplyService {

    /**
     * 一键发布
     */
    InterData oneButtonPublish(CandidateDTO dto, String remarks, boolean isForceApply) throws StatusMismatchException, ConfigExistException, ModifiedException, TemplateChangedException, IllegalTemplateException, OnePersonPublishException;

    void oneButtonPublishEvent(InterData interData, CandidateDTO dto, String remarks);

    void postPublicConfigEvent(ConfigMeta configMeta, String remarks);

    void rollBack(CandidateDTO dto, boolean isApprove);

    int batchSave(List<ConfigDetail> configDetails, boolean isPublic, boolean withPriority) throws FileNotFoundException, JsonProcessingException;

    /**
     * 强制提交，提交时不检查文件是否存在或已被删除，因为这个方法仅在创建一个与已删除的文件同名的文件时调用，
     * 直接把已删除的文件状态改为“待审核”，并更新其版本号和snapshot，流程与更新一个已发布的版本一致
     */
    void forceApply(CandidateDTO dto, String remarks) throws StatusMismatchException, ModifiedException, TemplateChangedException, IllegalTemplateException;

    /**
     * 申请
     */
    void apply(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException, ConfigExistException, TemplateChangedException, IllegalTemplateException;

    /**
     * 拷贝
     */
    void copyApply(CandidateDTO dto, ConfigInfoWithoutPublicStatus srcInfo, String remarks) throws ModifiedException, StatusMismatchException, ConfigExistException, TemplateChangedException, IllegalTemplateException;

    /**
     * 拒绝
     */
    void reject(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException, OnePersonPublishException;

    /**
     * 审核
     */
    void approve(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException, OnePersonPublishException;

    /**
     * 取消审核
     */
    void cancel(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException, OnePersonPublishException;

    /**
     * 发布
     */
    void publish(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException, OnePersonPublishException;

    /**
     * 删除(逻辑删除)
     */
    void delete(CandidateDTO dto, String remarks) throws ModifiedException, StatusMismatchException;

    /**
     * 推送
     */
    void push(CandidateDTO dto, String remarks, List<PushItemWithHostName> destinations) throws StatusMismatchException;

    /**
     * 编辑中推送
     */
    void pushEditing(CandidateDTO dto, String remarks, List<PushItemWithHostName> destinations) throws StatusMismatchException;

    /**
     * 回滚编辑中推送到最新推送版本
     */
    void rollBackEditPush(CandidateDTO dto) throws StatusMismatchException;

    /**
     *
     * 使文件变为公开状态
     */
    boolean makePublic(ConfigMeta configMeta, String remarks);

    /**
     * 使文件变为继承状态
     *
     * @return
     */
    boolean makeInherit(ConfigMeta configMeta, String remarks);

    /**
     * 使文件变为rest状态
     *
     * @return
     */
    boolean makeRest(ConfigMeta configMeta, String remarks);

    void postCurrentConfigChangedEvent(final CandidateDTO dto, final ConfigOperationEvent event);
}
