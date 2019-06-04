package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaRequest;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ReferenceService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;


@Controller
@RequestMapping("/qconfig/reference")
public class ReferenceFileController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(ReferenceFileController.class);

    @Resource(name = "eventPostReferenceService")
    private ReferenceService referenceService;

    @Resource
    private UserContextService userContextService;

    @RequestMapping("/list")
    @ResponseBody
    public Object referenceList(@RequestParam String group, @RequestParam String profile,
                                @RequestParam(required = false) String groupLike,
                                @RequestParam(required = false) String dataIdLike,
                                @RequestParam(required = false, defaultValue = "1") int page,
                                @RequestParam(required = false, defaultValue = "15") int pageSize) {
        checkLegalGroup(group);
        checkLegalProfile(profile);
        try {
            return JsonV2.successOf(
                    referenceService.getReferenceInfo(
                            group, profile, groupLike, dataIdLike, page, pageSize, true));
        } catch (RuntimeException e) {
            logger.error("get reference list error, group={}, profile={}", group, profile, e);
            throw e;
        }
    }

    @RequestMapping("/add")
    @ResponseBody
    public Object reference(@RequestBody Reference reference) {
        if (StringUtils.isEmpty(reference.getAlias())) {
            reference.setAlias(reference.getRefDataId());
        }
        check(reference);
        reference.setOperator(userContextService.getRtxId());
        logger.info("make reference, {}", reference);
        Monitor.REFERENCE_STATICS.inc();
        try {
            referenceService.addReference(reference);
            return JsonV2.successOf(true);
        } catch (ModifiedException e) {
            throw new RuntimeException("该文件已被引用过或文件别名已存在！");
        } catch (ConfigExistException e) {
            throw new RuntimeException("该文件名已存在或被引用过！");
        } catch (RuntimeException e) {
            logger.error("do reference error, {}", reference, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    @RequestMapping("/cancel")
    @ResponseBody
    public Object cancelRef(@RequestBody Reference reference) {
        checkNotNullOrEmpty(reference);
        reference.setOperator(userContextService.getRtxId());
        logger.info("remove reference, {}", reference);
        Monitor.CANCEL_REFERENCE_STATICS.inc();
        try {
            referenceService.removeReference(reference);
            return JsonV2.successOf(true);
        } catch (RuntimeException e) {
            logger.error("remove reference error, {}", reference, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    private void check(Reference reference) {
        checkNotNullOrEmpty(reference);
        FileChecker.checkName(reference.getAlias());
        FileChecker.checkName(reference.getRefDataId());
        checkArgument(!reference.getGroup().equals(reference.getRefGroup())
                || !reference.getProfile().equals(reference.getRefProfile()), "不能引用同group同profile配置！");
        boolean aliasTemplate = FileChecker.isTemplateFile(reference.getAlias());
        boolean refTemplate = FileChecker.isTemplateFile(reference.getRefDataId());
        checkArgument(aliasTemplate == refTemplate, "普通文件和模版文件不能相互引用");
    }

    private void checkNotNullOrEmpty(Reference reference) {
        checkArgument(!Strings.isNullOrEmpty(reference.getGroup()), "group不能为空!");
        checkArgument(ProfileUtil.legalProfile(reference.getProfile()), "无效的profile!");
        checkArgument(!Strings.isNullOrEmpty(reference.getAlias()), "文件别名不能为空！");
        checkArgument(!Strings.isNullOrEmpty(reference.getRefGroup()), "引用的group不能为空!");
        checkArgument(ProfileUtil.legalProfile(reference.getRefProfile()), "无效的引用profile！");
        checkArgument(!Strings.isNullOrEmpty(reference.getRefDataId()), "引用dataId不能为空！");
    }

    @RequestMapping("/referencedBy")
    @ResponseBody
    public Object findConfigsRefer(@RequestBody FileMetaRequest candidate) {
        checkLegalMeta(candidate);
        try {
            return JsonV2.successOf(
                    referenceService.findConfigsRefer(
                            new ConfigMeta(candidate.getGroup(), candidate.getDataId(), candidate.getProfile()),
                            RefType.REFERENCE));
        } catch (RuntimeException e) {
            logger.error("find reference by error, {}", candidate, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }
}
