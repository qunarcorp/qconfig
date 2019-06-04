package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaRequest;
import qunar.tc.qconfig.admin.service.ReferenceService;
import qunar.tc.qconfig.admin.service.impl.InheritConfigServiceImpl;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;


@Controller
@RequestMapping("/qconfig/inherit")
public class InheritController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(InheritController.class);

    @Resource
    private InheritConfigServiceImpl inheritConfigService;

    @Resource(name = "eventPostReferenceService")
    private ReferenceService referenceService;

    @RequestMapping(value = "/view/parentFile/exist")
    @ResponseBody
    public Object parentFileExists(@RequestParam(defaultValue="") String group,
                                   @RequestParam(defaultValue="") String dataId,
                                   @RequestParam(defaultValue="") String profile) {

        if(Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId) || Strings.isNullOrEmpty(profile)) {
            return new JsonV2<>(-1, "group=[" + group + "], dataId=[" + dataId + "], profile=['" + profile +"'] + 为空", null);
        }

        if(!FileChecker.isPropertiesFile(dataId)) {
            return new JsonV2<>(-1, "group=[" + group + "], dataId=[" + dataId + "], profile=['" + profile +"'] + 继承只支持properties类型配置文件", null);
        }

        boolean result = inheritConfigService.parentFileExists(group, dataId, profile);
        if(!result) {
            return new JsonV2<>(-1, "group=[" + group + "], dataId=[" + dataId + "], profile=[" + profile + "] + 不存在", null);
        }
        return true;
    }

    @RequestMapping(value = "/view/childFile/exist", method = RequestMethod.GET)
    @ResponseBody
    public Object childFileExists(@RequestParam(value="group", defaultValue="") String group,
                                  @RequestParam(value="dataId", defaultValue="") String dataId,
                                  @RequestParam(value="profile", defaultValue="") String profile) {
        if(Strings.isNullOrEmpty(group) || Strings.isNullOrEmpty(dataId) || Strings.isNullOrEmpty(profile)) {
            return new JsonV2<>(-1, "group=[" + group + "], dataId=[" + dataId + "], profile=['" + profile +"'] + 为空", null);
        }

        if(inheritConfigService.childFileExists(group, dataId, profile)) {
            return new JsonV2<>(-1, "子文件：" + dataId + "已经存在", null);
        }
        return true;
    }

    @RequestMapping(value = "/view/parentFiles", method = RequestMethod.GET)
    @ResponseBody
    public Object parentFiles(@RequestParam(value="group") String group,
                              @RequestParam(value="profile") String profile,
                              @RequestParam(value="term", required=false) String term,
                              @RequestParam(value="pageSize", required=false, defaultValue = "1000") int pageSize,
                              @RequestParam(value="curPage", required=false, defaultValue = "1") int curPage) {
        checkArgument(pageSize != 0, "pageSize不能为0");
        if (curPage <= 0) {
            curPage = 1;
        }
        return JsonV2.successOf(inheritConfigService.countParentFile(group, profile, term, curPage, pageSize));
    }

    @RequestMapping("/inheritedBy")
    @ResponseBody
    public Object findConfigsRefer(@RequestBody FileMetaRequest candidate) {
        checkLegalMeta(candidate);
        try {
            return JsonV2.successOf(referenceService.findConfigsRefer(
                    new ConfigMeta(candidate.getGroup(), candidate.getDataId(), candidate.getProfile()),
                    RefType.INHERIT));
        } catch (RuntimeException e) {
            logger.error("find inherited by error, {}", candidate, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }
}
