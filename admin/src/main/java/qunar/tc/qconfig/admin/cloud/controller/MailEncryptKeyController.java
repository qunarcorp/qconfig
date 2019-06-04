package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import qunar.tc.qconfig.admin.model.EncryptKey;
import qunar.tc.qconfig.admin.model.EncryptKeyStatus;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.EncryptKeyService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.bean.EncryptInfoBean;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.servercommon.util.QCloudUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/qconfig/file/encrypt")
public class MailEncryptKeyController extends AbstractControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(MailEncryptKeyController.class);

    @Resource
    private EncryptKeyService encryptKeyService;

    @Resource
    private UserContextService userContext;

    @RequestMapping("/view")
    @ResponseBody
    public Object encryptList(@RequestParam String group, @RequestParam String dataId, @RequestParam String profile, @RequestParam int editVersion) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            Map<String, Object> resultMap = Maps.newHashMap();
            resultMap.put("group", QCloudUtils.getAppFromGroup(group));
            resultMap.put("dataId", dataId);
            if (!dataId.endsWith(".properties")) {
                resultMap.put("encrypts", ImmutableMap.of());
            } else {
                Map<String, Boolean> encryptInfos = encryptKeyService.getEditableEncryptKeys(group, dataId, profile, editVersion);
                resultMap.put("encrypts", encryptInfos);
            }
            return JsonV2.successOf(resultMap);
        } catch (RuntimeException e) {
            logger.error("get encrypt list error, group={}, dataId={}, profile={}, editVersion={}", group, dataId, profile, editVersion, e);
            throw e;
        }
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public Object editEncryptList(@RequestParam String group, @RequestParam String dataId, final @RequestBody EncryptInfoBean[] encryptInfoBeans) {
        Monitor.ENCRYPT_KEY_STATICS.inc();
        String operator = userContext.getRtxId();
        List<EncryptKey> encryptKeys = new ArrayList<>();
        for (EncryptInfoBean encryptInfoBean : encryptInfoBeans) {
            encryptKeys.add(encryptInfoBeanToEncryptKey(encryptInfoBean));
        }
        encryptKeyService.insertOrUpdate(group, dataId, operator, encryptKeys);
        return JsonV2.success();
    }

    private EncryptKey encryptInfoBeanToEncryptKey(EncryptInfoBean encryptInfoBean) {
        return new EncryptKey(encryptInfoBean.getKey(), encryptInfoBean.isEncrypted() ? EncryptKeyStatus.ENCRYPTED : EncryptKeyStatus.PUBLIC);
    }
}
